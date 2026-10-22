package com.takenokoshi.overdriveae.main.registries;

import com.takenokoshi.overdriveae.main.OverdriveAE;
import com.takenokoshi.overdriveae.main.menu.OverdriveInscriberMenu;
import com.takenokoshi.overdriveae.main.menu.OverdriveMachineMenu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class OverdriveAEMenus {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU,
            OverdriveAE.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<OverdriveMachineMenu>> OVERDRIVE_MACHINE = MENU_TYPES
            .register("overdrive_machine", () -> OverdriveMachineMenu.TYPE);
    public static final DeferredHolder<MenuType<?>, MenuType<OverdriveInscriberMenu>> OVERDRIVE_INSCRIBER = MENU_TYPES
            .register("overdrive_inscriber", () -> OverdriveInscriberMenu.TYPE);
}
