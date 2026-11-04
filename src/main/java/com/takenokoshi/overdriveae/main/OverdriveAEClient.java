package com.takenokoshi.overdriveae.main;

import com.takenokoshi.overdriveae.main.menu.OverdriveInscriberMenu;
import com.takenokoshi.overdriveae.main.menu.OverdriveMachineMenu;
import com.takenokoshi.overdriveae.main.menu.OverdriveTransformChamberMenu;
import com.takenokoshi.overdriveae.main.screen.OverdriveInscriberScreen;
import com.takenokoshi.overdriveae.main.screen.OverdriveMachineScreen;
import com.takenokoshi.overdriveae.main.screen.OverdriveTransformChamberScreen;

import appeng.init.client.InitScreens;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = OverdriveAE.MODID, dist = Dist.CLIENT)
public class OverdriveAEClient extends OverdriveAE {
    public OverdriveAEClient(IEventBus modEventBus, ModContainer container) {
        super(modEventBus, container);
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        modEventBus.addListener(OverdriveAEClient::registerScreens);
    }


    private static void registerScreens(RegisterMenuScreensEvent event) {
        InitScreens.register(event, OverdriveTransformChamberMenu.TYPE, OverdriveTransformChamberScreen::new,
                "/screens/overdriveae/overdrive_inscriber.json");
        InitScreens.register(event, OverdriveInscriberMenu.TYPE, OverdriveInscriberScreen::new,
                "/screens/overdriveae/overdrive_inscriber.json");
        /** そのままだと型推論がうまくいかないのでクラス指定しておく */
        InitScreens.register(event, OverdriveMachineMenu.TYPE, OverdriveMachineScreen<OverdriveMachineMenu>::new ,
                "/screens/overdriveae/overdrive_machine.json");
    }
}
