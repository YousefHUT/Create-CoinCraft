package com.yousefhut.createcoincraft.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.yousefhut.createcoincraft.item.MoneyPouchItem;
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
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("coincraft", "textures/gui/pouch_gui.png");
    private EditBox amountField;
    private Button withdrawButton;
    private Button unitToggleButton;
    private int inputAmount = 0;
    private boolean useCogs = false;

    public MoneyPouchScreen(MoneyPouchMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 80;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        this.amountField = new EditBox(this.font, x + (this.imageWidth / 2) - 25, y + 20, 50, 10, Component.literal(""));
        this.amountField.setFilter(s -> s.isEmpty() || s.matches("\\d+"));
        this.amountField.setResponder(this::onAmountChanged);
        this.addRenderableWidget(this.amountField);

        this.withdrawButton = Button.builder(Component.literal("Withdraw"), this::onWithdraw)
                .bounds(x + (this.imageWidth / 2) - 30, y + 40, 60, 20)
                .build();
        this.addRenderableWidget(this.withdrawButton);

        this.unitToggleButton = Button.builder(getUnitToggleButtonText(), this::onUnitToggle)
                .bounds(x + (this.imageWidth / 2) + 30, y + 20 - 2, 60, 14)
                .build();
        this.addRenderableWidget(this.unitToggleButton);

        updateWithdrawButtonState();
    }

    private Component getUnitToggleButtonText() {
        return Component.literal("Unit: " + (useCogs ? "Cogs" : "Spurs"));
    }

    private void onUnitToggle(Button button) {
        useCogs = !useCogs;
        button.setMessage(getUnitToggleButtonText());
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
        graphics.drawString(this.font, this.title, this.imageWidth / 2 - this.font.width(this.title) / 2, 6, 4210752, false);

        int balance = this.menu.getBalance();
        String formattedBalanceText;

        if (balance > 0) {
            int cogs = balance / 64;
            int spurs = balance % 64;

            StringBuilder formattedBalance = new StringBuilder("Value: ");
            if (cogs > 0) {
                formattedBalance.append(cogs).append(" Cogs");
                if (spurs > 0) {
                    formattedBalance.append(", ");
                }
            }
            if (spurs > 0) {
                formattedBalance.append(spurs).append(" Spurs");
            }
            formattedBalance.append(" (").append(balance).append(")");

            formattedBalanceText = formattedBalance.toString();
        } else {
            formattedBalanceText = Component.translatable("item.coincraft.money_pouch.tooltip.empty").getString();
        }

        graphics.drawString(this.font, formattedBalanceText, this.imageWidth / 2 - this.font.width(formattedBalanceText) / 2, 70, 4210752, false);
    }

    private void onAmountChanged(String text) {
        try {
            this.inputAmount = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            this.inputAmount = 0;
        }
        updateWithdrawButtonState();
    }

    private void onWithdraw(Button button) {
        int effectiveWithdrawalAmount = inputAmount;
        if (useCogs) {
            effectiveWithdrawalAmount *= 64;
        }

        if (effectiveWithdrawalAmount > 0) {
            PacketDistributor.sendToServer(new WithdrawPacket(effectiveWithdrawalAmount, this.menu.getPouchSlotIndex()));
            this.minecraft.player.closeContainer();
        }
    }

    private void updateWithdrawButtonState() {
        int effectiveWithdrawalAmount = inputAmount;
        if (useCogs) {
            effectiveWithdrawalAmount *= 64;
        }
        this.withdrawButton.active = effectiveWithdrawalAmount > 0 && effectiveWithdrawalAmount <= this.menu.getBalance() && effectiveWithdrawalAmount <= MoneyPouchItem.MAX_BALANCE;
    }
}