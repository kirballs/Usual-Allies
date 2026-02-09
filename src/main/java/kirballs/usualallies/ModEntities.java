package kirballs.usualallies;

import kirballs.usualallies.entity.kirb.KirbEntity;
import kirballs.usualallies.projectile.FriendHeartProjectile;
import kirballs.usualallies.projectile.StarProjectile;
import kirballs.usualallies.projectile.AirBulletProjectile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registry class for all mod entities.
 * Add new entities here by creating new RegistryObject entries.
 */
public class ModEntities {
    // Deferred register for entity types
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, UsualAllies.MOD_ID);

    // Kirb entity registration
    // Customize the hitbox size (width, height) and other properties as needed
    // The model's main body is ~13.6 units (0.85 blocks), so hitbox is slightly smaller
    public static final RegistryObject<EntityType<KirbEntity>> KIRB =
            ENTITY_TYPES.register("kirb", () -> EntityType.Builder.of(KirbEntity::new, MobCategory.CREATURE)
                    .sized(0.7f, 0.7f) // Width and height of the hitbox - slightly smaller than the model
                    .clientTrackingRange(10) // How far away players can see this entity
                    .updateInterval(3) // How often the entity updates position to clients (in ticks)
                    .build(new ResourceLocation(UsualAllies.MOD_ID, "kirb").toString()));

    // Friend Heart projectile registration
    public static final RegistryObject<EntityType<FriendHeartProjectile>> FRIEND_HEART_PROJECTILE =
            ENTITY_TYPES.register("friend_heart_projectile", () -> EntityType.Builder.<FriendHeartProjectile>of(FriendHeartProjectile::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f) // Small projectile hitbox
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build(new ResourceLocation(UsualAllies.MOD_ID, "friend_heart_projectile").toString()));

    // Star projectile registration (for Kirb's spit attack)
    public static final RegistryObject<EntityType<StarProjectile>> STAR_PROJECTILE =
            ENTITY_TYPES.register("star_projectile", () -> EntityType.Builder.<StarProjectile>of(StarProjectile::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f) // Medium-sized projectile
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build(new ResourceLocation(UsualAllies.MOD_ID, "star_projectile").toString()));

    // Air Bullet projectile registration (for after flying)
    public static final RegistryObject<EntityType<AirBulletProjectile>> AIR_BULLET_PROJECTILE =
            ENTITY_TYPES.register("air_bullet_projectile", () -> EntityType.Builder.<AirBulletProjectile>of(AirBulletProjectile::new, MobCategory.MISC)
                    .sized(0.4f, 0.4f) // Air bullet size
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build(new ResourceLocation(UsualAllies.MOD_ID, "air_bullet_projectile").toString()));
}
