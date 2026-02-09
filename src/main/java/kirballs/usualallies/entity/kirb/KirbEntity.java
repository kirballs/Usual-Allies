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
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Kirb entity class - main entity representing Kirby in Minecraft.
 * Uses GeckoLib for animations and models.
 * 
 * Features:
 * - Inhale attack that pulls in enemies
 * - Can be befriended by feeding melon blocks or cake
 * - Has 3 lives system
 * - Can fly by flapping arms
 * - Can be dyed different colors when befriended
 */
public class KirbEntity extends TamableAnimal implements GeoEntity {
    
    // === SYNCHED DATA ACCESSORS ===
    // These are synchronized between server and client
    
    // Color of Kirb's body (dye color ordinal, -1 for default pink)
    private static final EntityDataAccessor<Integer> DATA_COLOR =
            SynchedEntityData.defineId(KirbEntity.class, EntityDataSerializers.INT);
    
    // Number of lives remaining (default 3)
    private static final EntityDataAccessor<Integer> DATA_LIVES =
            SynchedEntityData.defineId(KirbEntity.class, EntityDataSerializers.INT);
    
    // Current command state (follow, stay, wander, patrol)
    private static final EntityDataAccessor<Integer> DATA_COMMAND =
            SynchedEntityData.defineId(KirbEntity.class, EntityDataSerializers.INT);
    
    // Whether Kirb is currently inhaling
    private static final EntityDataAccessor<Boolean> DATA_INHALING =
            SynchedEntityData.defineId(KirbEntity.class, EntityDataSerializers.BOOLEAN);
    
    // Whether Kirb has an entity in his mouth
    private static final EntityDataAccessor<Boolean> DATA_HAS_CAPTURED =
            SynchedEntityData.defineId(KirbEntity.class, EntityDataSerializers.BOOLEAN);
    
    // Whether Kirb is currently flying (puffed up with air)
    private static final EntityDataAccessor<Boolean> DATA_FLYING =
            SynchedEntityData.defineId(KirbEntity.class, EntityDataSerializers.BOOLEAN);
    
    // Health state for face texture (0=full, 1=medium, 2=low)
    private static final EntityDataAccessor<Integer> DATA_HEALTH_STATE =
            SynchedEntityData.defineId(KirbEntity.class, EntityDataSerializers.INT);

    // === CONSTANTS ===
    
    // Inhale attack range in blocks - targets within this range get pulled
    private static final double INHALE_RANGE = 3.0; // 2-3 blocks
    
    // Mouth hitbox range - entities within this get captured
    private static final double MOUTH_RANGE = 1.0; // <1 block
    
    // Damage per second while entity is in mouth (cactus-like)
    private static final float MOUTH_DAMAGE_PER_SECOND = 2.0f; // 1 heart per second
    
    // Star projectile damage in half-hearts
    private static final float STAR_DAMAGE = 6.0f; // 3 hearts
    
    // Maximum hitbox size for inhaleable entities (player-sized)
    private static final double MAX_INHALEABLE_SIZE = 0.9; // Player is 0.6 x 1.8
    
    // Default number of lives
    private static final int DEFAULT_LIVES = 3; // 3 lives like in Kirby games
    
    // Time in ticks before Kirb stops inhaling without capturing (5 seconds = 100 ticks)
    private static final int INHALE_TIMEOUT = 100;
    
    // Interval between arm flaps while flying (0.3 seconds = 6 ticks)
    private static final int FLAP_INTERVAL = 6;
    
    // Respawn delay in ticks after losing a life (3 minutes = 3600 ticks)
    private static final int RESPAWN_DELAY = 3600;

    // === INSTANCE VARIABLES ===
    
    // GeckoLib animation cache
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    
    // Entity currently captured in Kirb's mouth
    @Nullable
    private LivingEntity capturedEntity;
    
    // UUID of captured entity (for saving/loading)
    @Nullable
    private UUID capturedEntityUUID;
    
    // Timer for inhale attack
    private int inhaleTimer = 0;
    
    // Timer for mouth damage ticks
    private int mouthDamageTimer = 0;
    
    // Timer for respawn countdown
    private int respawnTimer = 0;
    
    // Timer for flapping while flying
    private int flapTimer = 0;
    
    // BlockPos for patrol center
    @Nullable
    private BlockPos patrolCenter;
    
    // Sound looping timer for inhale
    private int inhaleSoundTimer = 0;

    // === CONSTRUCTOR ===
    
    public KirbEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setTame(false);
    }

    // === ATTRIBUTE REGISTRATION ===
    
    /**
     * Creates the attribute supplier for Kirb.
     * Adjust these values to change Kirb's stats.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0) // 10 hearts - adjust as needed
                .add(Attributes.MOVEMENT_SPEED, 0.3) // Movement speed
                .add(Attributes.ATTACK_DAMAGE, 3.0) // Base attack damage
                .add(Attributes.FOLLOW_RANGE, 32.0); // How far Kirb tracks targets
    }

    // === DATA SYNCHRONIZATION ===
    
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_COLOR, -1); // -1 = default pink
        this.entityData.define(DATA_LIVES, DEFAULT_LIVES);
        this.entityData.define(DATA_COMMAND, AllyCommand.FOLLOW.ordinal());
        this.entityData.define(DATA_INHALING, false);
        this.entityData.define(DATA_HAS_CAPTURED, false);
        this.entityData.define(DATA_FLYING, false);
        this.entityData.define(DATA_HEALTH_STATE, 0);
    }

    // === AI GOALS ===
    
    @Override
    protected void registerGoals() {
        // Priority 0 - highest priority
        this.goalSelector.addGoal(0, new FloatGoal(this));
        
        // Priority 1 - sitting (when commanded to stay)
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        
        // Priority 2 - inhale attack
        this.goalSelector.addGoal(2, new KirbInhaleGoal(this));
        
        // Priority 3 - melee attack fallback
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, true));
        
        // Priority 4 - follow owner when tamed
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.0, 10.0f, 2.0f, false));
        
        // Priority 5 - breeding (not used but inherited)
        this.goalSelector.addGoal(5, new BreedGoal(this, 1.0));
        
        // Priority 6 - wander
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        
        // Priority 7 - look at player
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0f));
        
        // Priority 8 - random look around
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        // Target selectors
        // When tamed, help owner in combat
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        
        // Retaliate when attacked
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        
        // When not tamed, target monsters that get too close
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Monster.class, true) {
            @Override
            public boolean canUse() {
                return !KirbEntity.this.isTame() && super.canUse();
            }
        });
    }

    // === TICK METHODS ===
    
    @Override
    public void tick() {
        super.tick();
        
        // Update health state for face texture
        updateHealthState();
        
        // Handle respawn timer if waiting to respawn
        if (respawnTimer > 0) {
            respawnTimer--;
            if (respawnTimer == 0) {
                performRespawn();
            }
        }
        
        // Handle inhale mechanics
        if (isInhaling()) {
            handleInhale();
        }
        
        // Handle captured entity damage
        if (hasCapturedEntity()) {
            handleCapturedEntity();
        }
        
        // Handle flying mechanics
        if (isFlying()) {
            handleFlying();
        }
    }
    
    /**
     * Updates the health state for face texture changes.
     * 0 = full health, 1 = medium health, 2 = low health
     */
    private void updateHealthState() {
        float healthPercent = getHealth() / getMaxHealth();
        int state;
        if (healthPercent > 0.6f) {
            state = 0; // Full health
        } else if (healthPercent > 0.3f) {
            state = 1; // Medium health
        } else {
            state = 2; // Low health
        }
        
        if (getHealthState() != state) {
            setHealthState(state);
        }
    }
    
    /**
     * Handles the inhale vacuum effect.
     */
    private void handleInhale() {
        inhaleTimer++;
        
        // Play looping inhale sound
        if (!level().isClientSide && inhaleSoundTimer <= 0) {
            level().playSound(null, this, ModSounds.KIRB_INHALE.get(), SoundSource.NEUTRAL, 1.0f, 1.0f);
            inhaleSoundTimer = 20; // Replay every second
        }
        inhaleSoundTimer--;
        
        // Stop inhaling after timeout
        if (inhaleTimer > INHALE_TIMEOUT) {
            setInhaling(false);
            inhaleTimer = 0;
            return;
        }
        
        // Get entities in range
        Vec3 lookVec = this.getLookAngle();
        AABB pullArea = this.getBoundingBox().inflate(INHALE_RANGE);
        List<LivingEntity> nearbyEntities = level().getEntitiesOfClass(LivingEntity.class, pullArea,
                entity -> entity != this && canInhale(entity));
        
        for (LivingEntity entity : nearbyEntities) {
            // Check if entity is in front of Kirb (within 90 degree cone)
            Vec3 toEntity = entity.position().subtract(this.position()).normalize();
            double dot = lookVec.dot(toEntity);
            
            if (dot > 0.5) { // Entity is roughly in front
                double distance = this.distanceTo(entity);
                
                // Pull entity towards Kirb
                if (distance > MOUTH_RANGE) {
                    // Calculate pull strength based on distance
                    // Maximum pull of 0.15 when closest, decreasing with distance
                    // Players can escape by sprinting (movement speed > 0.15)
                    double pullStrength = 0.15 * (1.0 - distance / INHALE_RANGE);
                    Vec3 pullVec = this.position().subtract(entity.position()).normalize().scale(pullStrength);
                    entity.setDeltaMovement(entity.getDeltaMovement().add(pullVec));
                } else {
                    // Entity is close enough to capture
                    captureEntity(entity);
                }
            }
        }
    }
    
    /**
     * Handles damage to captured entity and spit/swallow decision.
     */
    private void handleCapturedEntity() {
        if (capturedEntity == null || !capturedEntity.isAlive()) {
            // Entity died, perform swallow
            if (capturedEntity != null && !capturedEntity.isAlive()) {
                performSwallow();
            }
            releaseCapturedEntity();
            return;
        }
        
        // Position captured entity inside Kirb (invisible)
        capturedEntity.setPos(this.getX(), this.getY(), this.getZ());
        capturedEntity.setInvisible(true);
        capturedEntity.setInvulnerable(true);
        
        // Apply lingering damage every second
        mouthDamageTimer++;
        if (mouthDamageTimer >= 20) { // Every second
            capturedEntity.setInvulnerable(false);
            capturedEntity.hurt(level().damageSources().cactus(), MOUTH_DAMAGE_PER_SECOND);
            capturedEntity.setInvulnerable(true);
            mouthDamageTimer = 0;
        }
        
        // Check if should spit (another hostile nearby)
        if (shouldSpitEntity()) {
            spitEntity();
        }
    }
    
    /**
     * Handles flying mechanics (arm flapping).
     */
    private void handleFlying() {
        flapTimer++;
        
        // Slow falling while flying
        if (this.getDeltaMovement().y < -0.1) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.6, 1.0));
        }
        
        // Flap arms periodically
        if (flapTimer >= FLAP_INTERVAL) {
            flapTimer = 0;
            // Small upward boost
            this.setDeltaMovement(this.getDeltaMovement().add(0, 0.15, 0));
            
            // Play flap sound
            if (!level().isClientSide) {
                playSound(ModSounds.KIRB_FLAP.get(), 0.5f, 1.0f);
            }
        }
        
        // Check if landed
        if (this.onGround()) {
            stopFlying();
        }
    }

    // === CAPTURE AND SPIT MECHANICS ===
    
    /**
     * Checks if an entity can be inhaled based on size.
     */
    public boolean canInhale(LivingEntity entity) {
        if (entity instanceof Player && isTame()) {
            return false; // Don't inhale owner
        }
        
        // Check hitbox size
        float width = entity.getBbWidth();
        float height = entity.getBbHeight();
        
        // Max size is roughly player-sized (0.6 x 1.8)
        return width <= MAX_INHALEABLE_SIZE && height <= MAX_INHALEABLE_SIZE * 2;
    }
    
    /**
     * Captures an entity in Kirb's mouth.
     */
    public void captureEntity(LivingEntity entity) {
        if (!level().isClientSide) {
            this.capturedEntity = entity;
            this.capturedEntityUUID = entity.getUUID();
            this.setHasCaptured(true);
            this.setInhaling(false);
            this.inhaleTimer = 0;
            
            // Play capture sound
            playSound(ModSounds.KIRB_CAPTURE.get(), 1.0f, 1.0f);
        }
    }
    
    /**
     * Checks if Kirb should spit out the captured entity (another hostile nearby).
     */
    private boolean shouldSpitEntity() {
        if (capturedEntity == null) return false;
        
        // Look for other hostile entities nearby
        AABB searchArea = this.getBoundingBox().inflate(5.0);
        List<LivingEntity> nearbyHostiles = level().getEntitiesOfClass(LivingEntity.class, searchArea,
                entity -> entity != this && entity != capturedEntity && isHostile(entity));
        
        return !nearbyHostiles.isEmpty();
    }
    
    /**
     * Checks if an entity is hostile towards Kirb.
     */
    private boolean isHostile(LivingEntity entity) {
        if (entity instanceof Monster) return true;
        if (entity instanceof Mob mob) {
            return mob.getTarget() == this || (isTame() && mob.getTarget() == getOwner());
        }
        return false;
    }
    
    /**
     * Spits out the captured entity as a star projectile.
     */
    private void spitEntity() {
        if (level().isClientSide || capturedEntity == null) return;
        
        // Play spit sound
        playSound(ModSounds.KIRB_SPIT_STAR.get(), 1.0f, 1.0f);
        
        // Create star projectile
        StarProjectile star = new StarProjectile(ModEntities.STAR_PROJECTILE.get(), this, level());
        star.setPos(this.getX(), this.getEyeY() - 0.1, this.getZ());
        
        Vec3 direction = this.getLookAngle();
        star.shoot(direction.x, direction.y, direction.z, 1.5f, 0);
        
        level().addFreshEntity(star);
        
        // Release captured entity (kill it as it was converted to star)
        if (capturedEntity != null) {
            capturedEntity.setInvisible(false);
            capturedEntity.setInvulnerable(false);
            capturedEntity.discard();
        }
        releaseCapturedEntity();
    }
    
    /**
     * Performs the swallow animation/action after entity dies in mouth.
     */
    private void performSwallow() {
        if (!level().isClientSide) {
            playSound(ModSounds.KIRB_SWALLOW.get(), 1.0f, 1.0f);
            // Animation is handled by GeckoLib
        }
    }
    
    /**
     * Releases the captured entity reference.
     */
    private void releaseCapturedEntity() {
        if (capturedEntity != null) {
            capturedEntity.setInvisible(false);
            capturedEntity.setInvulnerable(false);
        }
        capturedEntity = null;
        capturedEntityUUID = null;
        setHasCaptured(false);
        mouthDamageTimer = 0;
    }

    // === FLYING MECHANICS ===
    
    /**
     * Starts flying mode (puff up with air).
     */
    public void startFlying() {
        setFlying(true);
        flapTimer = 0;
    }
    
    /**
     * Stops flying and shoots air bullet.
     */
    public void stopFlying() {
        if (isFlying() && !level().isClientSide) {
            setFlying(false);
            
            // Shoot air bullet
            playSound(ModSounds.KIRB_EXHALE.get(), 1.0f, 1.0f);
            
            AirBulletProjectile airBullet = new AirBulletProjectile(ModEntities.AIR_BULLET_PROJECTILE.get(), this, level());
            airBullet.setPos(this.getX(), this.getEyeY(), this.getZ());
            
            Vec3 direction = this.getLookAngle();
            airBullet.shoot(direction.x, direction.y - 0.3, direction.z, 0.5f, 5);
            
            level().addFreshEntity(airBullet);
        }
    }

    // === DAMAGE AND LIVES SYSTEM ===
    
    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        
        // Check for death
        if (this.getHealth() <= 0 && getLives() > 0) {
            // Use a life instead of dying
            setLives(getLives() - 1);
            
            if (getLives() > 0) {
                // Start respawn timer
                this.setHealth(1.0f);
                this.setInvisible(true);
                this.setInvulnerable(true);
                respawnTimer = RESPAWN_DELAY;
            }
        }
        
        return result;
    }
    
    /**
     * Performs respawn after death with remaining lives.
     */
    private void performRespawn() {
        this.setHealth(this.getMaxHealth());
        this.setInvisible(false);
        this.setInvulnerable(false);
        
        // Teleport next to owner if tamed
        if (isTame() && getOwner() != null) {
            this.teleportTo(getOwner().getX(), getOwner().getY(), getOwner().getZ());
        }
    }

    // === INTERACTION ===
    
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        
        // Check for taming items (melon block or cake)
        if (!isTame() && (itemStack.is(Items.MELON) || itemStack.is(Items.CAKE))) {
            if (!level().isClientSide) {
                if (!player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
                
                // Tame Kirb
                this.tame(player);
                this.navigation.stop();
                this.setTarget(null);
                this.setOrderedToSit(false);
                level().broadcastEntityEvent(this, (byte) 7); // Heart particles
            }
            return InteractionResult.SUCCESS;
        }
        
        // Check for dye (only when tamed)
        if (isTame() && isOwnedBy(player) && itemStack.getItem() instanceof DyeItem dyeItem) {
            DyeColor color = dyeItem.getDyeColor();
            if (getColor() != color.getId()) {
                if (!level().isClientSide) {
                    setColor(color.getId());
                    if (!player.getAbilities().instabuild) {
                        itemStack.shrink(1);
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }
        
        // Check for 1-Up item
        if (isTame() && isOwnedBy(player) && itemStack.is(ModItems.ONE_UP.get())) {
            if (!level().isClientSide) {
                setLives(getLives() + 1);
                playSound(ModSounds.KIRB_ONE_UP.get(), 1.0f, 1.0f);
                if (!player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
            }
            return InteractionResult.SUCCESS;
        }
        
        // Toggle command when tamed and empty hand
        if (isTame() && isOwnedBy(player) && itemStack.isEmpty()) {
            if (!level().isClientSide) {
                AllyCommand newCommand = cycleCommand();
                // Send message to player indicating new command
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable("entity.usualallies.kirb.command." + newCommand.name().toLowerCase()),
                        true); // true = action bar message
            }
            return InteractionResult.SUCCESS;
        }
        
        return super.mobInteract(player, hand);
    }
    
    /**
     * Cycles through command states.
     * @return The new command after cycling
     */
    private AllyCommand cycleCommand() {
        AllyCommand current = getCommand();
        AllyCommand next = AllyCommand.values()[(current.ordinal() + 1) % AllyCommand.values().length];
        setCommand(next);
        
        // Apply command effects
        switch (next) {
            case STAY:
                this.setOrderedToSit(true);
                this.navigation.stop();
                break;
            case FOLLOW:
                this.setOrderedToSit(false);
                break;
            case WANDER:
                this.setOrderedToSit(false);
                break;
            case PATROL:
                this.setOrderedToSit(false);
                this.patrolCenter = this.blockPosition();
                break;
        }
        
        return next;
    }

    // === GECKOLIB ANIMATION ===
    
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 10, this::predicate));
    }
    
    private PlayState predicate(AnimationState<KirbEntity> state) {
        // Animation priorities
        if (hasCapturedEntity()) {
            // Has entity in mouth - could be idle or ready to spit
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.kirb.idle"));
        }
        
        if (isInhaling()) {
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.kirb.inhale"));
        }
        
        if (isFlying()) {
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.kirb.fly"));
        }
        
        if (this.getDeltaMovement().y < -0.1 && !this.onGround()) {
            // Falling - could trigger fly
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.kirb.jump"));
        }
        
        if (state.isMoving()) {
            // Check if sprinting speed
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

    // === BREEDING (DISABLED) ===
    
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null; // Kirb doesn't breed
    }

    // === DATA GETTERS AND SETTERS ===
    
    public int getColor() {
        return this.entityData.get(DATA_COLOR);
    }
    
    public void setColor(int color) {
        this.entityData.set(DATA_COLOR, color);
    }
    
    public int getLives() {
        return this.entityData.get(DATA_LIVES);
    }
    
    public void setLives(int lives) {
        this.entityData.set(DATA_LIVES, lives);
    }
    
    public AllyCommand getCommand() {
        return AllyCommand.values()[this.entityData.get(DATA_COMMAND)];
    }
    
    public void setCommand(AllyCommand command) {
        this.entityData.set(DATA_COMMAND, command.ordinal());
    }
    
    public boolean isInhaling() {
        return this.entityData.get(DATA_INHALING);
    }
    
    public void setInhaling(boolean inhaling) {
        this.entityData.set(DATA_INHALING, inhaling);
        if (!inhaling) {
            inhaleTimer = 0;
            inhaleSoundTimer = 0;
        }
    }
    
    public boolean hasCapturedEntity() {
        return this.entityData.get(DATA_HAS_CAPTURED);
    }
    
    public void setHasCaptured(boolean hasCaptured) {
        this.entityData.set(DATA_HAS_CAPTURED, hasCaptured);
    }
    
    public boolean isFlying() {
        return this.entityData.get(DATA_FLYING);
    }
    
    public void setFlying(boolean flying) {
        this.entityData.set(DATA_FLYING, flying);
    }
    
    public int getHealthState() {
        return this.entityData.get(DATA_HEALTH_STATE);
    }
    
    public void setHealthState(int state) {
        this.entityData.set(DATA_HEALTH_STATE, state);
    }
    
    @Nullable
    public LivingEntity getCapturedEntity() {
        return capturedEntity;
    }
    
    @Nullable
    public BlockPos getPatrolCenter() {
        return patrolCenter;
    }

    // === NBT SAVE/LOAD ===
    
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Color", getColor());
        tag.putInt("Lives", getLives());
        tag.putInt("Command", getCommand().ordinal());
        tag.putBoolean("Flying", isFlying());
        tag.putInt("HealthState", getHealthState());
        
        if (capturedEntityUUID != null) {
            tag.putUUID("CapturedEntity", capturedEntityUUID);
        }
        
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
        
        if (tag.hasUUID("CapturedEntity")) {
            capturedEntityUUID = tag.getUUID("CapturedEntity");
        }
        
        if (tag.contains("PatrolX")) {
            patrolCenter = new BlockPos(tag.getInt("PatrolX"), tag.getInt("PatrolY"), tag.getInt("PatrolZ"));
        }
    }

    // === SOUNDS ===
    
    @Override
    protected SoundEvent getAmbientSound() {
        return null; // No ambient sound - add custom if desired
    }
    
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return null; // Add custom hurt sound if desired
    }
    
    @Override
    protected SoundEvent getDeathSound() {
        return null; // Add custom death sound if desired
    }
    
    @Override
    protected void playStepSound(BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        this.playSound(ModSounds.KIRB_STEP.get(), 0.15f, 1.0f);
    }
    
    @Override
    protected void jumpFromGround() {
        super.jumpFromGround();
        if (!level().isClientSide) {
            playSound(ModSounds.KIRB_JUMP.get(), 0.5f, 1.0f);
        }
    }
}
