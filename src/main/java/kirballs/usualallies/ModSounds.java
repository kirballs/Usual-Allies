package kirballs.usualallies;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registry class for all mod sound events.
 *
 * Sound IDs follow Minecraft's own dot-separated convention
 * (e.g. entity.generic.hurt).  OGG files must be placed under
 * assets/usualallies/sounds/ using the corresponding
 * slash-separated sub-path.
 *
 * All entries are also declared in assets/usualallies/sounds.json.
 */
public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, UsualAllies.MOD_ID);

    // === KIRB SOUNDS ===

    // Footstep sound – plays on every step block
    // OGG: sounds/kirb/step.ogg
    public static final RegistryObject<SoundEvent> KIRB_STEP =
            registerSound("kirb.step");

    // Jump sound – plays when Kirb jumps
    // OGG: sounds/kirb/jump.ogg
    public static final RegistryObject<SoundEvent> KIRB_JUMP =
            registerSound("kirb.jump");

    // Walk-loop sound – plays each time the walk/run animation loops
    // OGG: sounds/kirb/walk.ogg
    public static final RegistryObject<SoundEvent> KIRB_WALK =
            registerSound("kirb.walk");

    // Flap sound – plays each time Kirb completes a flap cycle while flying
    // OGG: sounds/kirb/flap.ogg
    public static final RegistryObject<SoundEvent> KIRB_FLAP =
            registerSound("kirb.flap");

    // Exhale / air-bullet sound – plays when Kirb releases the air projectile
    // OGG: sounds/kirb/exhale.ogg
    public static final RegistryObject<SoundEvent> KIRB_EXHALE =
            registerSound("kirb.exhale");

    // Inhale charge-up – first beat of the inhale at animation start
    // OGG: sounds/kirb/inhale1.ogg
    public static final RegistryObject<SoundEvent> KIRB_INHALE_START =
            registerSound("kirb.inhale1");

    // Inhale loop – smooth loop that immediately follows kirb.inhale1
    // OGG: sounds/kirb/inhale2.ogg
    public static final RegistryObject<SoundEvent> KIRB_INHALE_LOOP =
            registerSound("kirb.inhale2");

    // Mouthful sound – plays the moment an entity enters Kirb's mouth
    // OGG: sounds/kirb/mouthful.ogg
    public static final RegistryObject<SoundEvent> KIRB_MOUTHFUL =
            registerSound("kirb.mouthful");

    // Spit sound – plays when the spit/star-launch animation starts
    // OGG: sounds/kirb/spit.ogg
    public static final RegistryObject<SoundEvent> KIRB_SPIT =
            registerSound("kirb.spit");

    // Swallow sound – plays when Kirb swallows a captured entity
    // OGG: sounds/kirb/swallow.ogg
    public static final RegistryObject<SoundEvent> KIRB_SWALLOW =
            registerSound("kirb.swallow");

    // Tamed sound – plays after befriending Kirb
    // OGG: sounds/kirb/tamed.ogg
    public static final RegistryObject<SoundEvent> KIRB_TAMED =
            registerSound("kirb.tamed");

    // 1-Up sound – plays when Kirb is given a 1-Up item
    // OGG: sounds/kirb/one_up.ogg
    public static final RegistryObject<SoundEvent> KIRB_ONE_UP =
            registerSound("kirb.one_up");

    // Hurt sounds – one is picked at random when Kirb takes damage
    // OGGs: sounds/kirb/hurt1.ogg, hurt2.ogg, hurt3.ogg
    public static final RegistryObject<SoundEvent> KIRB_HURT_1 =
            registerSound("kirb.hurt1");
    public static final RegistryObject<SoundEvent> KIRB_HURT_2 =
            registerSound("kirb.hurt2");
    public static final RegistryObject<SoundEvent> KIRB_HURT_3 =
            registerSound("kirb.hurt3");

    // === ALLY SOUNDS ===

    // Low-HP alert – plays when any ally drops into low-health state
    // OGG: sounds/ally/lowhp.ogg
    public static final RegistryObject<SoundEvent> ALLY_LOW_HP =
            registerSound("ally.lowhp");

    // === FRIEND HEART SOUNDS ===

    // Throw sound – plays when a Friend Heart is thrown
    // OGG: sounds/friend_heart/throw.ogg
    public static final RegistryObject<SoundEvent> FRIEND_HEART_THROW =
            registerSound("friend_heart.throw");

    // Hit sound – plays when a Friend Heart successfully converts a mob
    // OGG: sounds/friend_heart/hit.ogg
    public static final RegistryObject<SoundEvent> FRIEND_HEART_HIT =
            registerSound("friend_heart.hit");

    // -------------------------------------------------------------------------

    private static RegistryObject<SoundEvent> registerSound(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(
                new ResourceLocation(UsualAllies.MOD_ID, name)));
    }
}

