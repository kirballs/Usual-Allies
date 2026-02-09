package kirballs.usualallies;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registry class for all mod sound events.
 * Provide sound files in: assets/usualallies/sounds/
 * Sound format should be .ogg (Ogg Vorbis)
 * Also define sounds in: assets/usualallies/sounds.json
 */
public class ModSounds {
    // Deferred register for sound events
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, UsualAllies.MOD_ID);

    // === KIRB SOUNDS ===
    
    // Footstep sound - plays each time Kirb takes a step
    // File: assets/usualallies/sounds/kirb_step.ogg
    public static final RegistryObject<SoundEvent> KIRB_STEP =
            registerSound("kirb_step");

    // Jump sound - plays when Kirb jumps
    // File: assets/usualallies/sounds/kirb_jump.ogg
    public static final RegistryObject<SoundEvent> KIRB_JUMP =
            registerSound("kirb_jump");

    // Flap sound - plays each time Kirb flaps arms while flying
    // File: assets/usualallies/sounds/kirb_flap.ogg
    public static final RegistryObject<SoundEvent> KIRB_FLAP =
            registerSound("kirb_flap");

    // Air bullet exhale sound - plays when Kirb releases air projectile
    // File: assets/usualallies/sounds/kirb_exhale.ogg
    public static final RegistryObject<SoundEvent> KIRB_EXHALE =
            registerSound("kirb_exhale");

    // Inhale sound - loops while vacuum is active
    // File: assets/usualallies/sounds/kirb_inhale.ogg
    public static final RegistryObject<SoundEvent> KIRB_INHALE =
            registerSound("kirb_inhale");

    // Capture sound - plays when entity enters Kirb's mouth
    // File: assets/usualallies/sounds/kirb_capture.ogg
    public static final RegistryObject<SoundEvent> KIRB_CAPTURE =
            registerSound("kirb_capture");

    // Star spit sound - plays when Kirb shoots star projectile
    // File: assets/usualallies/sounds/kirb_spit_star.ogg
    public static final RegistryObject<SoundEvent> KIRB_SPIT_STAR =
            registerSound("kirb_spit_star");

    // Swallow sound - plays when Kirb swallows captured entity
    // File: assets/usualallies/sounds/kirb_swallow.ogg
    public static final RegistryObject<SoundEvent> KIRB_SWALLOW =
            registerSound("kirb_swallow");

    // 1-Up sound - plays when Kirb is fed a 1-Up item
    // File: assets/usualallies/sounds/kirb_one_up.ogg
    public static final RegistryObject<SoundEvent> KIRB_ONE_UP =
            registerSound("kirb_one_up");

    // === FRIEND HEART SOUNDS ===
    
    // Throw sound - plays when Friend Heart is thrown
    // File: assets/usualallies/sounds/friend_heart_throw.ogg
    public static final RegistryObject<SoundEvent> FRIEND_HEART_THROW =
            registerSound("friend_heart_throw");

    // Hit sound - plays when Friend Heart successfully hits a mob
    // File: assets/usualallies/sounds/friend_heart_hit.ogg
    public static final RegistryObject<SoundEvent> FRIEND_HEART_HIT =
            registerSound("friend_heart_hit");

    /**
     * Helper method to register a sound event.
     * @param name The name of the sound (without mod ID prefix)
     * @return The registered sound event
     */
    private static RegistryObject<SoundEvent> registerSound(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(
                new ResourceLocation(UsualAllies.MOD_ID, name)));
    }
}
