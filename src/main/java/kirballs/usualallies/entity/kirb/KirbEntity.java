package kirballs.usualallies.entity.kirb;

import kirballs.usualallies.ModEntities;
import kirballs.usualallies.ModItems;
import kirballs.usualallies.ModSounds;
import kirballs.usualallies.projectile.StarProjectile;
import kirballs.usualallies.projectile.AirBulletProjectile;
import kirballs.usualallies.util.AllyCommand;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Kirb entity – main entity representing Kirby in Minecraft.
 *
 * Features:
 * - Inhale vacuum attack that pulls and captures enemies (suction particles client-side)
 * - Player-capture escape mechanic: mash sprint/sneak 10-15 times to break free
 * - Random chance to expel a captured entity (not a player) at low health before it dies
 * - Pick-up / throw: owner can carry Kirb in hand and launch him like a projectile
 * - Star projectile when spitting a captured entity
 * - 3-lives system with respawn timer
 * - Fly mode with reduced gravity and arm-flap boost
 * - Dye support for body colour
 * - Separate body/face texture system (5 face states)
 * - Only retaliates against enemies who hurt the owner (no OwnerHurtTargetGoal)
 */
public class KirbEntity extends TamableAnimal implements GeoEntity {

    // =========================================================================
    // CARRY STATE CONSTANTS
    // =========================================================================
    /** Kirb is not being carried. */
    public static final int CARRY_NONE   = 0;
    /** Kirb is held in the owner's hand. */
    public static final int CARRY_HELD   = 1;
    /** Kirb is mid-air after being thrown. */
    public static final int CARRY_THROWN = 2;

    // =========================================================================
    // FACE STATE CONSTANTS  (must match KirbFaceLayer constants)
    // =========================================================================
    public static final int FACE_O        = 0;  // default / floating medium health
    public static final int FACE_HAPPY    = 1;  // tamed + high health
    public static final int FACE_MOUTHFUL = 2;  // entity captured until swallow end
    public static final int FACE_OPEN     = 3;  // inhaling / expelling star
    public static final int FACE_LOWHP    = 4;  // low health

    // =========================================================================
    // SYNCHED DATA ACCESSORS
    // =========================================================================
    private static final EntityDataAccessor<Integer> DATA_COLOR =
            SynchedEntityData.defineId(KirbEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_LIVES =
            SynchedEntityData.defineId(KirbEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_COMMAND =
            SynchedEntityData.defineId(KirbEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_INHALING =
            SynchedEntityData.defineId(KirbEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_HAS_CAPTURED =
            SynchedEntityData.defineId(KirbEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_FLYING =
            SynchedEntityData.defineId(KirbEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_HEALTH_STATE =
            SynchedEntityData.defineId(KirbEntity.class, EntityDataSerializers.INT);
    /** 0 = CARRY_NONE, 1 = CARRY_HELD, 2 = CARRY_THROWN */
    private static final EntityDataAccessor<Integer> DATA_CARRY_STATE =
            SynchedEntityData.defineId(KirbEntity.class, EntityDataSerializers.INT);
    /** One of the FACE_* constants – drives KirbFaceLayer texture choice. */
    private static final EntityDataAccessor<Integer> DATA_FACE_STATE =
            SynchedEntityData.defineId(KirbEntity.class, EntityDataSerializers.INT);
    /** True while the pushback animation should play (player escaped from mouth). */
    private static final EntityDataAccessor<Boolean> DATA_PUSHBACK =
            SynchedEntityData.defineId(KirbEntity.class, EntityDataSerializers.BOOLEAN);
    /** Entity ID of a captured *player*, or -1. Synced so the client can show the black-out. */
    private static final EntityDataAccessor<Integer> DATA_CAPTURED_ENTITY_ID =
            SynchedEntityData.defineId(KirbEntity.class, EntityDataSerializers.INT);

    // =========================================================================
    // CONSTANTS
    // =========================================================================
    private static final double INHALE_RANGE     = 3.0;
    private static final double MOUTH_RANGE      = 1.0;
    private static final float  MOUTH_DPS        = 2.0f; // 1 heart / second
    private static final float  STAR_DAMAGE      = 6.0f; // 3 hearts
    private static final double MAX_INHALEABLE_SIZE = 0.9;
    private static final int    DEFAULT_LIVES    = 3;
    private static final int    INHALE_TIMEOUT   = 100;
    private static final int    FLAP_INTERVAL    = 6;
    private static final int    RESPAWN_DELAY    = 3600;

    // Throw mechanic
    private static final float THROW_DAMAGE      = 3.0f;  // 1.5 hearts to target
    private static final float THROW_SELF_DAMAGE = 0.5f;  // 0.25 heart self-damage
    private static final float THROW_SPEED       = 1.8f;
    private static final int   THROW_MAX_AGE     = 40;    // 2 seconds max airtime

    // Player escape from mouth
    private static final int ESCAPE_PRESSES_MIN = 10;
    private static final int ESCAPE_PRESSES_MAX = 15;
    private static final int PUSHBACK_DURATION  = 12;     // ticks

    // Pre-death random expulsion
    private static final float EXPEL_CHANCE_PER_TICK  = 0.05f;
    private static final float EXPEL_HEALTH_THRESHOLD = 0.3f;

    // Walk-loop sound timing (matches animation lengths)
    private static final int WALK_SOUND_INTERVAL = 10; // 0.5 s walk loop
    private static final int RUN_SOUND_INTERVAL  = 6;  // 0.3 s run loop

    // Face-open lingers briefly after spitting
    private static final int SPIT_FACE_DURATION = 8;

    // =========================================================================
    // INSTANCE VARIABLES
    // =========================================================================
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Nullable private LivingEntity capturedEntity;
    @Nullable private UUID         capturedEntityUUID;

    private int inhaleTimer      = 0;
    private int mouthDamageTimer = 0;
    private int respawnTimer     = 0;
    private int flapTimer        = 0;
    @Nullable private BlockPos patrolCenter;
    private boolean inhaleStarted = false;
    private int inhaleSoundTimer  = 0;

    // Carry / throw
    private int holdingPlayerId = -1;
    @Nullable private UUID holdingPlayerUUID = null;
    private int thrownAge = 0;

    // Player escape from mouth
    private int     playerEscapeRequired    = 0;
    private int     playerEscapeCount       = 0;
    private boolean lastPlayerCrouching     = false;
    private boolean lastPlayerSprinting     = false;

    // Pushback animation timer (server-side countdown)
    private int pushbackTimer = 0;

    // Face state helpers
    private boolean wasLowHp       = false;
    private int     spitFaceTimer  = 0;

    // Walk loop sound
    private int walkSoundTimer = 0;

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================
    public KirbEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setTame(false);
    }

    // =========================================================================
    // ATTRIBUTES
    // =========================================================================
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)     // Same as a regular player
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    // =========================================================================
    // SYNCHED DATA INIT
    // =========================================================================
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_COLOR,       -1);
        this.entityData.define(DATA_LIVES,       DEFAULT_LIVES);
        this.entityData.define(DATA_COMMAND,     AllyCommand.FOLLOW.ordinal());
        this.entityData.define(DATA_INHALING,    false);
        this.entityData.define(DATA_HAS_CAPTURED, false);
        this.entityData.define(DATA_FLYING,      false);
        this.entityData.define(DATA_HEALTH_STATE, 0);
        this.entityData.define(DATA_CARRY_STATE, CARRY_NONE);
        this.entityData.define(DATA_FACE_STATE,  FACE_O);
        this.entityData.define(DATA_PUSHBACK,    false);
        this.entityData.define(DATA_CAPTURED_ENTITY_ID, -1);
    }

    // =========================================================================
    // AI GOALS
    // =========================================================================
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new KirbInhaleGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.0, 10.0f, 2.0f, false));
        this.goalSelector.addGoal(5, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        // Only retaliate against enemies who hurt the owner – no OwnerHurtTargetGoal.
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Monster.class, true) {
            @Override public boolean canUse() {
                return !KirbEntity.this.isTame() && super.canUse();
            }
        });
    }

    // =========================================================================
    // TICK
    // =========================================================================
    @Override
    public void tick() {
        super.tick();

        updateHealthState();
        updateFaceState();
        tickWalkSound();

        if (respawnTimer > 0 && --respawnTimer == 0) {
            performRespawn();
        }

        // Pushback animation countdown
        if (pushbackTimer > 0 && --pushbackTimer == 0) {
            setPushback(false);
        }

        // Spit-face linger countdown
        if (spitFaceTimer > 0) {
            spitFaceTimer--;
        }

        // Handle carry states
        int carry = getCarryState();
        if (carry == CARRY_HELD) {
            handleHeld();
        } else if (carry == CARRY_THROWN) {
            handleThrown();
        } else {
            // Normal behaviour
            if (isInhaling())       handleInhale();
            if (hasCapturedEntity()) handleCapturedEntity();
            if (isFlying())          handleFlying();
        }
    }

    // -------------------------------------------------------------------------
    // Carry – held in owner's hand
    // -------------------------------------------------------------------------
    private void handleHeld() {
        if (level().isClientSide) return;

        Player holder = findHoldingPlayer();
        if (holder == null) {
            // Player left or died – drop Kirb
            dropFromHold();
            return;
        }

        // Teleport Kirb to roughly the player's right-hand position
        Vec3 look  = holder.getLookAngle();
        Vec3 right = new Vec3(-look.z, 0, look.x).normalize();
        Vec3 handPos = holder.position()
                .add(0, holder.getEyeHeight() * 0.68, 0)
                .add(look.scale(0.35))
                .add(right.scale(0.38));

        this.teleportTo(handPos.x, handPos.y, handPos.z);
        this.setDeltaMovement(Vec3.ZERO);
        this.setNoGravity(true);
        this.setNoAi(true);
        this.setYRot(holder.getYRot());
        this.yBodyRot = holder.getYRot();
    }

    /**
     * Called (server-side) by {@link KirbCarryEvents} when the holding player
     * right-clicks empty space to launch Kirb.
     */
    public void throwKirb(Player thrower) {
        if (level().isClientSide) return;

        this.setNoGravity(false);
        this.setNoAi(false);
        setCarryState(CARRY_THROWN);
        holdingPlayerId = thrower.getId();

        Vec3 dir = thrower.getLookAngle();
        this.setDeltaMovement(dir.scale(THROW_SPEED));
        thrownAge = 0;
    }

    // -------------------------------------------------------------------------
    // Carry – flying as a thrown projectile
    // -------------------------------------------------------------------------
    private void handleThrown() {
        if (level().isClientSide) return;

        thrownAge++;
        if (thrownAge > THROW_MAX_AGE || this.onGround()) {
            landAfterThrow();
            return;
        }

        // Resolve the throwing player from the holding UUID
        Player thrower = null;
        if (holdingPlayerUUID != null && level() instanceof ServerLevel sl) {
            thrower = sl.getPlayerByUUID(holdingPlayerUUID);
        }
        final Player finalThrower = thrower;

        // Check for entity hits
        AABB hitBox = this.getBoundingBox().inflate(0.15);
        List<LivingEntity> hits = level().getEntitiesOfClass(LivingEntity.class, hitBox,
                e -> e != this && e != finalThrower && e.isAlive());

        if (!hits.isEmpty()) {
            LivingEntity target = hits.get(0);
            DamageSource src = level().damageSources().thrown(this,
                    thrower != null ? thrower : this);
            target.hurt(src, THROW_DAMAGE);
            // Quarter-heart self damage
            this.hurt(level().damageSources().generic(), THROW_SELF_DAMAGE);
            landAfterThrow();
        }
    }

    private void landAfterThrow() {
        setCarryState(CARRY_NONE);
        this.setNoGravity(false);
        this.setNoAi(false);
        thrownAge = 0;
    }

    private void dropFromHold() {
        setCarryState(CARRY_NONE);
        this.setNoGravity(false);
        this.setNoAi(false);
        holdingPlayerId  = -1;
        holdingPlayerUUID = null;
    }

    @Nullable
    private Player findHoldingPlayer() {
        if (holdingPlayerUUID != null && level() instanceof ServerLevel sl) {
            return sl.getPlayerByUUID(holdingPlayerUUID);
        }
        if (holdingPlayerId >= 0) {
            Entity e = level().getEntity(holdingPlayerId);
            if (e instanceof Player p) return p;
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Inhale
    // -------------------------------------------------------------------------
    private void handleInhale() {
        inhaleTimer++;

        if (!level().isClientSide) {
            if (!inhaleStarted) {
                // Play charge-up once at the start of inhaling
                level().playSound(null, this, ModSounds.KIRB_INHALE_START.get(),
                        SoundSource.NEUTRAL, 1.0f, 1.0f);
                inhaleStarted  = true;
                inhaleSoundTimer = 20;
            } else {
                inhaleSoundTimer--;
                if (inhaleSoundTimer <= 0) {
                    // Smooth looping portion
                    level().playSound(null, this, ModSounds.KIRB_INHALE_LOOP.get(),
                            SoundSource.NEUTRAL, 1.0f, 1.0f);
                    inhaleSoundTimer = 20;
                }
            }
        }

        if (inhaleTimer > INHALE_TIMEOUT) {
            setInhaling(false);
            return;
        }

        Vec3 lookVec  = this.getLookAngle();
        AABB pullArea = this.getBoundingBox().inflate(INHALE_RANGE);
        List<LivingEntity> nearby = level().getEntitiesOfClass(LivingEntity.class, pullArea,
                e -> e != this && canInhale(e));

        for (LivingEntity entity : nearby) {
            Vec3 toEntity = entity.position().subtract(this.position()).normalize();
            if (lookVec.dot(toEntity) > 0.5) {
                double dist = this.distanceTo(entity);
                if (dist > MOUTH_RANGE) {
                    double pull = 0.15 * (1.0 - dist / INHALE_RANGE);
                    entity.setDeltaMovement(entity.getDeltaMovement()
                            .add(this.position().subtract(entity.position()).normalize().scale(pull)));
                } else {
                    captureEntity(entity);
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Captured entity handling
    // -------------------------------------------------------------------------
    private void handleCapturedEntity() {
        if (capturedEntity == null || !capturedEntity.isAlive()) {
            if (capturedEntity != null && !capturedEntity.isAlive()) performSwallow();
            releaseCapturedEntity();
            return;
        }

        // Keep entity hidden inside Kirb
        capturedEntity.setPos(this.getX(), this.getY(), this.getZ());
        capturedEntity.setInvisible(true);
        capturedEntity.setInvulnerable(true);

        // Damage entity every second
        mouthDamageTimer++;
        if (mouthDamageTimer >= 20) {
            capturedEntity.setInvulnerable(false);
            capturedEntity.hurt(level().damageSources().cactus(), MOUTH_DPS);
            capturedEntity.setInvulnerable(true);
            mouthDamageTimer = 0;
        }

        // --- Player escape mechanic ---
        if (capturedEntity instanceof Player capturedPlayer) {
            trackPlayerEscape(capturedPlayer);
            return; // Players don't get auto-expelled or trigger spit targeting
        }

        // --- Pre-death random expulsion (non-player entities only) ---
        float maxHp = capturedEntity.getMaxHealth();
        float healthRatio = (maxHp > 0) ? capturedEntity.getHealth() / maxHp : 0f;
        if (healthRatio < EXPEL_HEALTH_THRESHOLD && random.nextFloat() < EXPEL_CHANCE_PER_TICK) {
            expelEntityBeforeDeath();
            return;
        }

        // --- Spit-to-attack if another hostile is nearby ---
        if (shouldSpitEntity()) {
            spitEntity();
        }
    }

    /**
     * Tracks sprint/sneak state toggles for a captured player.
     * After {@code playerEscapeRequired} toggles the player breaks free.
     */
    private void trackPlayerEscape(Player player) {
        boolean crouching = player.isCrouching();
        boolean sprinting = player.isSprinting();

        if (crouching != lastPlayerCrouching || sprinting != lastPlayerSprinting) {
            lastPlayerCrouching = crouching;
            lastPlayerSprinting = sprinting;
            onCapturedPlayerAction();
        }
    }

    /**
     * Called whenever the captured player performs any input action
     * (sprint/sneak toggle, punch, or right-click).
     * Increments the escape counter and releases the player once the
     * required number of presses is reached.
     */
    public void onCapturedPlayerAction() {
        if (!(capturedEntity instanceof Player)) return;
        playerEscapeCount++;
        if (playerEscapeCount >= playerEscapeRequired) {
            releaseCapturedEntity();
            if (!level().isClientSide) {
                setPushback(true);
                pushbackTimer = PUSHBACK_DURATION;
            }
        }
    }

    /**
     * Expels the captured entity in a random direction at significant speed,
     * simulating the entity being launched out before dying.
     */
    private void expelEntityBeforeDeath() {
        if (level().isClientSide || capturedEntity == null) return;

        capturedEntity.setInvisible(false);
        capturedEntity.setInvulnerable(false);

        // Random direction – full 3-D
        double yaw   = random.nextDouble() * Math.PI * 2;
        double pitch = (random.nextDouble() - 0.5) * Math.PI; // -90 to +90
        double hSpeed = Math.cos(pitch) * 1.6;
        double vx = Math.cos(yaw) * hSpeed;
        double vy = Math.sin(pitch) * 1.6 + 0.4;
        double vz = Math.sin(yaw) * hSpeed;
        capturedEntity.setDeltaMovement(vx, vy, vz);

        releaseCapturedEntity();
    }

    // -------------------------------------------------------------------------
    // Flying
    // -------------------------------------------------------------------------
    private void handleFlying() {
        flapTimer++;

        // Reduced gravity: dampen downward velocity
        Vec3 motion = this.getDeltaMovement();
        if (motion.y < 0) {
            this.setDeltaMovement(motion.multiply(1.0, 0.55, 1.0));
        }

        // Periodic flap – upward boost + sound
        if (flapTimer >= FLAP_INTERVAL) {
            flapTimer = 0;
            this.setDeltaMovement(this.getDeltaMovement().add(0, 0.18, 0));
            if (!level().isClientSide) {
                playSound(ModSounds.KIRB_FLAP.get(), 0.5f, 1.0f);
            }
        }

        if (this.onGround()) stopFlying();
    }

    // =========================================================================
    // CAPTURE / SPIT / SWALLOW
    // =========================================================================
    public boolean canInhale(LivingEntity entity) {
        // Never inhale the owner
        if (entity instanceof Player player && isTame() && isOwnedBy(player)) return false;
        return entity.getBbWidth() <= MAX_INHALEABLE_SIZE
                && entity.getBbHeight() <= MAX_INHALEABLE_SIZE * 2;
    }

    public void captureEntity(LivingEntity entity) {
        if (!level().isClientSide) {
            this.capturedEntity    = entity;
            this.capturedEntityUUID = entity.getUUID();
            this.setHasCaptured(true);
            this.setInhaling(false);
            this.inhaleTimer = 0;

            // Initialise player-escape counter if a player was swallowed
            if (entity instanceof Player) {
                playerEscapeRequired = ESCAPE_PRESSES_MIN
                        + random.nextInt(ESCAPE_PRESSES_MAX - ESCAPE_PRESSES_MIN + 1);
                playerEscapeCount    = 0;
                lastPlayerCrouching  = entity.isCrouching();
                lastPlayerSprinting  = entity.isSprinting();
                // Sync the player's entity ID so the client can apply the black-out
                this.entityData.set(DATA_CAPTURED_ENTITY_ID, entity.getId());
            }

            playSound(ModSounds.KIRB_MOUTHFUL.get(), 1.0f, 1.0f);
        }
    }

    private boolean shouldSpitEntity() {
        if (capturedEntity == null) return false;
        AABB area = this.getBoundingBox().inflate(5.0);
        return !level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != this && e != capturedEntity && isHostile(e)).isEmpty();
    }

    private boolean isHostile(LivingEntity entity) {
        if (entity instanceof Monster) return true;
        if (entity instanceof Mob mob) {
            return mob.getTarget() == this
                    || (isTame() && mob.getTarget() == getOwner());
        }
        return false;
    }

    private void spitEntity() {
        if (level().isClientSide || capturedEntity == null) return;

        playSound(ModSounds.KIRB_SPIT.get(), 1.0f, 1.0f);

        StarProjectile star = new StarProjectile(ModEntities.STAR_PROJECTILE.get(), this, level());
        star.setPos(this.getX(), this.getEyeY() - 0.1, this.getZ());
        Vec3 dir = this.getLookAngle();
        star.shoot(dir.x, dir.y, dir.z, 1.5f, 0);
        level().addFreshEntity(star);

        capturedEntity.setInvisible(false);
        capturedEntity.setInvulnerable(false);
        capturedEntity.discard();
        releaseCapturedEntity();

        // Keep face_open briefly after the spit
        spitFaceTimer = SPIT_FACE_DURATION;
    }

    private void performSwallow() {
        if (!level().isClientSide) {
            playSound(ModSounds.KIRB_SWALLOW.get(), 1.0f, 1.0f);
        }
    }

    private void releaseCapturedEntity() {
        if (capturedEntity != null) {
            capturedEntity.setInvisible(false);
            capturedEntity.setInvulnerable(false);
        }
        capturedEntity      = null;
        capturedEntityUUID  = null;
        setHasCaptured(false);
        this.entityData.set(DATA_CAPTURED_ENTITY_ID, -1);
        mouthDamageTimer    = 0;
        playerEscapeCount   = 0;
        playerEscapeRequired = 0;
    }

    // =========================================================================
    // FLYING
    // =========================================================================
    public void startFlying() {
        setFlying(true);
        flapTimer = 0;
    }

    public void stopFlying() {
        if (isFlying() && !level().isClientSide) {
            setFlying(false);
            playSound(ModSounds.KIRB_EXHALE.get(), 1.0f, 1.0f);

            AirBulletProjectile bullet = new AirBulletProjectile(
                    ModEntities.AIR_BULLET_PROJECTILE.get(), this, level());
            bullet.setPos(this.getX(), this.getEyeY(), this.getZ());
            Vec3 dir = this.getLookAngle();
            bullet.shoot(dir.x, dir.y - 0.3, dir.z, 0.5f, 5);
            level().addFreshEntity(bullet);
        }
    }

    // =========================================================================
    // HEALTH STATE & FACE STATE
    // =========================================================================
    /**
     * Updates {@code DATA_HEALTH_STATE} (0=full, 1=medium, 2=low) and triggers
     * the {@code ally.lowhp} sound the first time low-HP is entered.
     */
    private void updateHealthState() {
        float pct = getHealth() / getMaxHealth();
        int state = (pct > 0.6f) ? 0 : (pct > 0.3f) ? 1 : 2;

        if (getHealthState() != state) {
            setHealthState(state);
            // One-shot low-HP alert
            if (state == 2 && !wasLowHp && !level().isClientSide) {
                level().playSound(null, this,
                        ModSounds.ALLY_LOW_HP.get(), SoundSource.NEUTRAL, 1.0f, 1.0f);
            }
        }
        wasLowHp = (state == 2);
    }

    /** Derives the correct face from current entity state and syncs it. */
    private void updateFaceState() {
        int face;
        if (hasCapturedEntity()) {
            face = FACE_MOUTHFUL;
        } else if (isInhaling() || spitFaceTimer > 0) {
            face = FACE_OPEN;
        } else if (getHealthState() == 2) {
            face = FACE_LOWHP;
        } else if (isTame() && getHealthState() == 0) {
            face = FACE_HAPPY;
        } else {
            face = FACE_O;
        }
        if (getFaceState() != face) setFaceState(face);
    }

    // =========================================================================
    // WALK-LOOP SOUND
    // =========================================================================
    private void tickWalkSound() {
        if (!level().isClientSide && this.onGround() && getCarryState() == CARRY_NONE) {
            double hSpeed = this.getDeltaMovement().horizontalDistance();
            if (hSpeed > 0.02) {
                // Running uses a shorter loop interval
                int interval = (hSpeed > 0.15) ? RUN_SOUND_INTERVAL : WALK_SOUND_INTERVAL;
                walkSoundTimer++;
                if (walkSoundTimer >= interval) {
                    walkSoundTimer = 0;
                    playSound(ModSounds.KIRB_WALK.get(), 0.6f, hSpeed > 0.15 ? 1.2f : 1.0f);
                }
            } else {
                walkSoundTimer = 0;
            }
        }
    }

    // =========================================================================
    // DAMAGE & LIVES
    // =========================================================================
    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (this.getHealth() <= 0 && getLives() > 0) {
            setLives(getLives() - 1);
            if (getLives() > 0) {
                this.setHealth(1.0f);
                this.setInvisible(true);
                this.setInvulnerable(true);
                respawnTimer = RESPAWN_DELAY;
            }
        }
        return result;
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource source) {
        // 50% chance to play a random hurt sound
        if (random.nextFloat() < 0.5f) {
            int r = random.nextInt(3);
            if (r == 0) return ModSounds.KIRB_HURT_1.get();
            if (r == 1) return ModSounds.KIRB_HURT_2.get();
            return ModSounds.KIRB_HURT_3.get();
        }
        return null;
    }

    private void performRespawn() {
        this.setHealth(this.getMaxHealth());
        this.setInvisible(false);
        this.setInvulnerable(false);
        if (isTame() && getOwner() != null) {
            this.teleportTo(getOwner().getX(), getOwner().getY(), getOwner().getZ());
        }
    }

    // =========================================================================
    // INTERACTION
    // =========================================================================
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // --- Taming ---
        if (!isTame() && (stack.is(Items.MELON) || stack.is(Items.CAKE))) {
            if (!level().isClientSide) {
                if (!player.getAbilities().instabuild) stack.shrink(1);
                this.tame(player);
                this.navigation.stop();
                this.setTarget(null);
                this.setOrderedToSit(false);
                level().broadcastEntityEvent(this, (byte) 7);
                playSound(ModSounds.KIRB_TAMED.get(), 1.0f, 1.0f);
            }
            return InteractionResult.SUCCESS;
        }

        if (isTame() && isOwnedBy(player)) {
            // --- Dye ---
            if (stack.getItem() instanceof DyeItem dyeItem) {
                DyeColor color = dyeItem.getDyeColor();
                int targetColor = (color == DyeColor.PINK) ? -1 : color.getId();
                if (getColor() != targetColor && !level().isClientSide) {
                    setColor(targetColor);
                    if (!player.getAbilities().instabuild) stack.shrink(1);
                }
                return InteractionResult.SUCCESS;
            }

            // --- 1-Up ---
            if (stack.is(ModItems.ONE_UP.get())) {
                if (!level().isClientSide) {
                    setLives(getLives() + 1);
                    playSound(ModSounds.KIRB_ONE_UP.get(), 1.0f, 1.0f);
                    if (!player.getAbilities().instabuild) stack.shrink(1);
                }
                return InteractionResult.SUCCESS;
            }

            // --- Empty hand ---
            if (stack.isEmpty()) {
                if (!level().isClientSide) {
                    if (player.isCrouching()) {
                        // Sneak + interact → pick up Kirb
                        pickUpByPlayer(player);
                    } else {
                        // Normal interact → cycle command
                        AllyCommand cmd = cycleCommand();
                        player.displayClientMessage(
                                net.minecraft.network.chat.Component.translatable(
                                        "entity.usualallies.kirb.command." + cmd.name().toLowerCase()),
                                true);
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }

        return super.mobInteract(player, hand);
    }

    /**
     * Picks Kirb up so he's held in the player's hand.
     * Any currently-captured entity is released first.
     */
    private void pickUpByPlayer(Player player) {
        if (hasCapturedEntity()) releaseCapturedEntity();
        setInhaling(false);

        holdingPlayerUUID = player.getUUID();
        holdingPlayerId   = player.getId();
        setCarryState(CARRY_HELD);
    }

    private AllyCommand cycleCommand() {
        AllyCommand next = AllyCommand.values()[
                (getCommand().ordinal() + 1) % AllyCommand.values().length];
        setCommand(next);
        switch (next) {
            case STAY    -> { this.setOrderedToSit(true);  this.navigation.stop(); }
            case FOLLOW  -> this.setOrderedToSit(false);
            case WANDER  -> this.setOrderedToSit(false);
            case PATROL  -> { this.setOrderedToSit(false); this.patrolCenter = this.blockPosition(); }
        }
        return next;
    }

    // =========================================================================
    // GECKOLIB ANIMATION
    // =========================================================================
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::predicate));
    }

    private PlayState predicate(AnimationState<KirbEntity> state) {
        // Pushback (player escaped) – highest priority
        if (isPushback()) {
            return state.setAndContinue(RawAnimation.begin()
                    .then("animation.kirb.pushback", Animation.LoopType.PLAY_ONCE));
        }

        // Held or thrown – static tucked pose
        if (getCarryState() != CARRY_NONE) {
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.kirb.held"));
        }

        // Ordered sit / stay mode – static sitting pose
        if (this.isOrderedToSit()) {
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.kirb.sit"));
        }

        // Entity in mouth – puffed idle
        if (hasCapturedEntity()) {
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.kirb.mouthful"));
        }

        if (isInhaling()) {
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.kirb.inhale"));
        }

        if (isFlying()) {
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.kirb.fly"));
        }

        if (this.getDeltaMovement().y < -0.1 && !this.onGround()) {
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.kirb.jump"));
        }

        if (state.isMoving()) {
            if (this.getDeltaMovement().horizontalDistance() > 0.15) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.kirb.run"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.kirb.walk"));
        }

        return state.setAndContinue(RawAnimation.begin().thenLoop("animation.kirb.idle"));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // =========================================================================
    // BREEDING (DISABLED)
    // =========================================================================
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        return null;
    }

    // =========================================================================
    // SOUNDS
    // =========================================================================
    @Override
    @Nullable
    protected SoundEvent getAmbientSound() { return null; }

    @Override
    @Nullable
    protected SoundEvent getDeathSound() { return null; }

    @Override
    protected void playStepSound(BlockPos pos,
            net.minecraft.world.level.block.state.BlockState state) {
        this.playSound(ModSounds.KIRB_STEP.get(), 0.15f, 1.0f);
    }

    @Override
    protected void jumpFromGround() {
        super.jumpFromGround();
        if (!level().isClientSide) {
            playSound(ModSounds.KIRB_JUMP.get(), 0.5f, 1.0f);
        }
    }

    // =========================================================================
    // GETTERS / SETTERS
    // =========================================================================
    public int  getColor()       { return this.entityData.get(DATA_COLOR); }
    public void setColor(int v)  { this.entityData.set(DATA_COLOR, v); }

    public int  getLives()       { return this.entityData.get(DATA_LIVES); }
    public void setLives(int v)  { this.entityData.set(DATA_LIVES, v); }

    public AllyCommand getCommand()           { return AllyCommand.values()[this.entityData.get(DATA_COMMAND)]; }
    public void setCommand(AllyCommand cmd)   { this.entityData.set(DATA_COMMAND, cmd.ordinal()); }

    public boolean isInhaling()              { return this.entityData.get(DATA_INHALING); }
    public void setInhaling(boolean v) {
        this.entityData.set(DATA_INHALING, v);
        if (!v) { inhaleTimer = 0; inhaleSoundTimer = 0; inhaleStarted = false; }
    }

    public boolean hasCapturedEntity()       { return this.entityData.get(DATA_HAS_CAPTURED); }
    public void setHasCaptured(boolean v)    { this.entityData.set(DATA_HAS_CAPTURED, v); }

    public boolean isFlying()                { return this.entityData.get(DATA_FLYING); }
    public void setFlying(boolean v)         { this.entityData.set(DATA_FLYING, v); }

    public int  getHealthState()             { return this.entityData.get(DATA_HEALTH_STATE); }
    public void setHealthState(int v)        { this.entityData.set(DATA_HEALTH_STATE, v); }

    public int  getCarryState()              { return this.entityData.get(DATA_CARRY_STATE); }
    public void setCarryState(int v)         { this.entityData.set(DATA_CARRY_STATE, v); }

    public int  getFaceState()               { return this.entityData.get(DATA_FACE_STATE); }
    public void setFaceState(int v)          { this.entityData.set(DATA_FACE_STATE, v); }

    public boolean isPushback()              { return this.entityData.get(DATA_PUSHBACK); }
    public void setPushback(boolean v)       { this.entityData.set(DATA_PUSHBACK, v); }

    /** Entity ID of the currently captured player, or -1 if none. Used client-side for the black-out. */
    public int getCapturedEntityId()         { return this.entityData.get(DATA_CAPTURED_ENTITY_ID); }

    @Nullable public LivingEntity getCapturedEntity() { return capturedEntity; }
    @Nullable public BlockPos     getPatrolCenter()   { return patrolCenter; }
    public int getHoldingPlayerId()                   { return holdingPlayerId; }
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Color",       getColor());
        tag.putInt("Lives",       getLives());
        tag.putInt("Command",     getCommand().ordinal());
        tag.putBoolean("Flying",  isFlying());
        tag.putInt("HealthState", getHealthState());
        tag.putInt("CarryState",  getCarryState());

        if (capturedEntityUUID != null) tag.putUUID("CapturedEntity", capturedEntityUUID);
        if (holdingPlayerUUID  != null) tag.putUUID("HoldingPlayer",  holdingPlayerUUID);

        if (patrolCenter != null) {
            tag.putInt("PatrolX", patrolCenter.getX());
            tag.putInt("PatrolY", patrolCenter.getY());
            tag.putInt("PatrolZ", patrolCenter.getZ());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setColor(tag.getInt("Color"));
        setLives(tag.contains("Lives") ? tag.getInt("Lives") : DEFAULT_LIVES);
        setCommand(AllyCommand.values()[tag.getInt("Command")]);
        setFlying(tag.getBoolean("Flying"));
        setHealthState(tag.getInt("HealthState"));
        setCarryState(tag.getInt("CarryState"));

        if (tag.hasUUID("CapturedEntity")) capturedEntityUUID = tag.getUUID("CapturedEntity");
        if (tag.hasUUID("HoldingPlayer"))  holdingPlayerUUID  = tag.getUUID("HoldingPlayer");

        if (tag.contains("PatrolX")) {
            patrolCenter = new BlockPos(
                    tag.getInt("PatrolX"), tag.getInt("PatrolY"), tag.getInt("PatrolZ"));
        }
    }
}
