### File: src/main/java/com/yousefhut/createcoincraft/CoinCraft.java
```java
package com.yousefhut.createcoincraft;

import com.yousefhut.createcoincraft.core.ModDataComponents;
import com.yousefhut.createcoincraft.core.ModItems;
import com.yousefhut.createcoincraft.core.ModMenuTypes;
import com.yousefhut.createcoincraft.network.PacketHandler;
import com.yousefhut.createcoincraft.screen.MoneyPouchScreen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(CoinCraft.MODID)
public class CoinCraft {
    public static final String MODID = "coincraft";

    public CoinCraft(IEventBus modEventBus) {
        ModItems.ITEMS.register(modEventBus);
        ModDataComponents.DATA_COMPONENTS.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);

        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(PacketHandler::register);
        modEventBus.addListener(this::onRegisterMenuScreens);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Client setup tasks
        });
    }

    private void onRegisterMenuScreens(final RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.MONEY_POUCH_MENU.get(), MoneyPouchScreen::new);
    }
}
```
---
### File: src/main/java/com/yousefhut/createcoincraft/item/MoneyPouchItem.java
```java
package com.yousefhut.createcoincraft.item;

import com.yousefhut.createcoincraft.core.ModDataComponents;
import com.yousefhut.createcoincraft.menu.MoneyPouchMenu;
import com.yousefhut.createcoincraft.core.ModMenuTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class MoneyPouchItem extends Item {

    public MoneyPouchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            ((ServerPlayer) player).openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return stack.getHoverName();
                }

                @Nullable
                @Override
                public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
                    return new MoneyPouchMenu(ModMenuTypes.MONEY_POUCH_MENU.get(), windowId, playerInventory, stack);
                }
            }, buf -> ItemStack.STREAM_CODEC.encode(buf, stack));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }


    @Override
    public boolean overrideOtherStackedOnMe(ItemStack pouchStack, ItemStack carriedStack, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY) {
            return false;
        }

        if (carriedStack.isEmpty()) {
            return false;
        }

        // if (carriedStack.getItem() instanceof CoinItem coinItem) {
        //     long currentBalance = Optional.ofNullable(pouchStack.get(ModDataComponents.BALANCE)).orElse(0L);
        //     Coin coin = coinItem.getCoin();
        //     long value = (long) coin.value * carriedStack.getCount();
        //
        //     pouchStack.set(ModDataComponents.BALANCE, currentBalance + value);
        //
        //     playInsertSound(player);
        //     carriedStack.setCount(0);
        //     return true;
        // }

        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        long balance = Optional.ofNullable(stack.get(ModDataComponents.BALANCE)).orElse(0L);
        tooltip.add(Component.translatable("item.coincraft.money_pouch.tooltip", balance));
        super.appendHoverText(stack, context, tooltip, flag);
    }

    private void playInsertSound(Player player) {
        player.level().playSound(player, player.blockPosition(), SoundEvents.BUNDLE_INSERT, SoundSource.PLAYERS, 0.8F, 1.0F);
    }
}
```
---
### File: src/main/java/com/yousefhut/createcoincraft/menu/MoneyPouchMenu.java
```java
package com.yousefhut.createcoincraft.menu;

import com.yousefhut.createcoincraft.core.ModDataComponents;
import com.yousefhut.createcoincraft.core.ModItems;
import com.yousefhut.createcoincraft.core.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class MoneyPouchMenu extends AbstractContainerMenu {
    public final ItemStack pouch;
    private final Player player;

    public MoneyPouchMenu(@Nullable MenuType<?> type, int windowId, Inventory playerInventory, ItemStack pouch) {
        super(type, windowId);
        this.pouch = pouch;
        this.player = playerInventory.player;

        // Player Inventory
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        // Player Hotbar
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    public static MoneyPouchMenu create(int windowId, Inventory playerInventory, FriendlyByteBuf data) {
        ItemStack pouch = ItemStack.STREAM_CODEC.decode((RegistryFriendlyByteBuf) data);
        return new MoneyPouchMenu(ModMenuTypes.MONEY_POUCH_MENU.get(), windowId, playerInventory, pouch);
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return this.pouch.is(ModItems.MONEY_POUCH.get());
    }

    public long getBalance() {
        return this.pouch.getOrDefault(ModDataComponents.BALANCE, 0L);
    }
}
```
---
### File: src/main/java/com/yousefhut/createcoincraft/screen/MoneyPouchScreen.java
```java
package com.yousefhut.createcoincraft.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.yousefhut.createcoincraft.menu.MoneyPouchMenu;
import com.yousefhut.createcoincraft.network.WithdrawPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class MoneyPouchScreen extends AbstractContainerScreen<MoneyPouchMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/container/generic_54.png");
    private EditBox amountField;
    private Button withdrawButton;
    private long withdrawalAmount = 0;

    public MoneyPouchScreen(MoneyPouchMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        this.amountField = new EditBox(this.font, x + 62, y + 20, 50, 10, Component.literal(""));
        this.amountField.setFilter(s -> s.isEmpty() || s.matches("\\d+"));
        this.amountField.setResponder(this::onAmountChanged);
        this.addRenderableWidget(this.amountField);

        this.withdrawButton = Button.builder(Component.literal("Withdraw"), this::onWithdraw)
                .bounds(x + 58, y + 40, 60, 20)
                .build();
        this.addRenderableWidget(this.withdrawButton);

        updateWithdrawButtonState();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
        graphics.drawString(this.font, "Balance: " + this.menu.getBalance(), this.titleLabelX, this.titleLabelY + 10, 4210752, false);
    }

    private void onAmountChanged(String text) {
        try {
            this.withdrawalAmount = Long.parseLong(text);
        } catch (NumberFormatException e) {
            this.withdrawalAmount = 0;
        }
        updateWithdrawButtonState();
    }

    private void onWithdraw(Button button) {
        if (this.withdrawalAmount > 0) {
            PacketDistributor.sendToServer(new WithdrawPacket(this.withdrawalAmount));
        }
    }

    private void updateWithdrawButtonState() {
        this.withdrawButton.active = this.withdrawalAmount > 0 && this.withdrawalAmount <= this.menu.getBalance();
    }
}
```
---
### Resource Structure: src/main/resources
```
assets
data
META-INF
pack.mcmeta
pack.png
```
---

### Analysis Summary:

1.  **Item Interaction (Bundle-like behavior):**
    *   The `MoneyPouchItem.java` class correctly overrides `overrideOtherStackedOnMe`. However, the `overrideStackedOnOther` method is missing. For full bundle-like interaction, both methods are usually required.
    *   The commented-out code block within `overrideOtherStackedOnMe` suggests an intention to handle `CoinItem`s, but it's currently inactive. This needs to be uncommented and `CoinItem` and `Coin` classes need to be defined and implemented for the logic to work.

2.  **GUI/Screen:**
    *   **Broken GUI (background issues):** In `MoneyPouchScreen.java`, the `TEXTURE` is set to `minecraft:textures/gui/container/generic_54.png`. This is a generic vanilla texture. To have a custom GUI background, you need to create your own texture and reference it here.
    *   **Hide player inventory slots:** In `MoneyPouchMenu.java`, the player inventory slots are explicitly added in the constructor. To hide these, you would either remove these `addSlot` calls or adjust their positions to be outside the visible screen area, and then adjust the `imageHeight` in `MoneyPouchScreen` accordingly. If you want to hide them completely, removing the `addSlot` calls is the way to go.

3.  **Assets:**
    *   **Item Texture:** Based on the `src/main/resources` structure, your assets are organized under `assets/coincraft/`. For an item texture, it should typically be placed in `src/main/resources/assets/coincraft/textures/item/`. For example, `src/main/resources/assets/coincraft/textures/item/money_pouch.png`.
    *   **Linking Item Texture:** The item texture is usually linked via a JSON model file. You would need a file like `src/main/resources/assets/coincraft/models/item/money_pouch.json` that points to your texture.
        Example `money_pouch.json`:
        ```json
        {
          "parent": "item/generated",
          "textures": {
            "layer0": "coincraft:item/money_pouch"
          }
        }
        ```
        And then ensure your `ModItems` class registers the item correctly so the model is picked up.
