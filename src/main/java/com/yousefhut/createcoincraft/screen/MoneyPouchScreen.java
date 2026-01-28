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
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("coincraft", "textures/gui/pouch_gui.png");
    private EditBox amountField;
    private Button withdrawButton;
    private long withdrawalAmount = 0;

    public MoneyPouchScreen(MoneyPouchMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176; // Smaller width
        this.imageHeight = 80; // Smaller height
        this.inventoryLabelY = this.imageHeight - 94; // Adjust inventory label position if needed, though no inventory slots
    }

    @Override
    protected void init() {
        super.init();
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Center the EditBox
        this.amountField = new EditBox(this.font, x + (this.imageWidth / 2) - 25, y + 20, 50, 10, Component.literal(""));
        this.amountField.setFilter(s -> s.isEmpty() || s.matches("\\d+"));
        this.amountField.setResponder(this::onAmountChanged);
        this.addRenderableWidget(this.amountField);

        // Center the Withdraw button
        this.withdrawButton = Button.builder(Component.literal("Withdraw"), this::onWithdraw)
                .bounds(x + (this.imageWidth / 2) - 30, y + 40, 60, 20)
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
        // Adjust title and balance display positions
        graphics.drawString(this.font, this.title, this.imageWidth / 2 - this.font.width(this.title) / 2, 6, 4210752, false);
        graphics.drawString(this.font, "Balance: " + this.menu.getBalance(), this.imageWidth / 2 - this.font.width("Balance: " + this.menu.getBalance()) / 2, 70, 4210752, false);
        // No player inventory title needed as there are no slots
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
