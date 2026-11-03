package com.takenokoshi.overdriveae.main.screen;

import java.util.function.Supplier;

import appeng.client.gui.Icon;
import appeng.client.gui.widgets.IconButton;
import net.minecraft.network.chat.Component;

public class BasicIconButton extends IconButton {

    private final Supplier<Icon> supplier;

    public BasicIconButton(OnPress onPress, Supplier<Icon> supplier) {
        super(onPress);
        this.supplier = supplier;
    }

    @Override
    protected Icon getIcon() {
        return supplier.get();
    }

    public BasicIconButton setText(Component message) {
        setMessage(message);
        return this;
    }

}
