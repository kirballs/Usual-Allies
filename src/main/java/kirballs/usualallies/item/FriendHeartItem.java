package kirballs.usualallies.item;

import kirballs.usualallies.ModSounds;
import kirballs.usualallies.projectile.FriendHeartProjectile;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/**
 * Friend Heart item - throwable item that befriends hostile mobs.
 * 
 * Usage:
 * - Hold right-click to aim (like a bow)
 * - Release to throw the heart
 * - The heart travels 3-4 blocks max
 * - On hitting a mob, it becomes friendly to the player
 */
public class FriendHeartItem extends Item {

    // How long to charge for maximum throw distance (in ticks)
    private static final int MAX_CHARGE_TIME = 20; // 1 second

    public FriendHeartItem(Properties properties) {
        super(properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        // Use bow animation for aiming
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        // Maximum time the item can be held
        return 72000; // Same as bow
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        
        // Start charging/aiming
        player.startUsingItem(hand);
        
        return InteractionResultHolder.consume(itemStack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, net.minecraft.world.entity.LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) {
            return;
        }

        // Calculate charge time
        int chargeTime = this.getUseDuration(stack) - timeLeft;
        
        // Only throw if charged enough (at least 5 ticks)
        if (chargeTime < 5) {
            return;
        }

        if (!level.isClientSide) {
            // Calculate throw power based on charge time (0.5 to 1.0)
            float power = Math.min(chargeTime / (float) MAX_CHARGE_TIME, 1.0f);
            power = 0.5f + power * 0.5f;

            // Create and shoot the friend heart projectile
            FriendHeartProjectile projectile = new FriendHeartProjectile(level, player);
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, power * 1.5f, 1.0f);
            level.addFreshEntity(projectile);

            // Play throw sound
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.FRIEND_HEART_THROW.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        }

        // Consume the item (unless in creative mode)
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        // Add stat
        player.awardStat(Stats.ITEM_USED.get(this));
    }
}
