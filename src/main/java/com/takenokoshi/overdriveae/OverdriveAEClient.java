package com.takenokoshi.overdriveae;

import com.takenokoshi.overdriveae.menu.OverdriveInscriberMenu;
import com.takenokoshi.overdriveae.menu.OverdriveMachineMenu;
import com.takenokoshi.overdriveae.screen.OverdriveInscriberScreen;
import com.takenokoshi.overdriveae.screen.OverdriveMachineScreen;

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


    /**
     * 画面の登録。1.20.5 で専用イベント {@link RegisterMenuScreensEvent} が新設され、
     * {@code FMLClientSetupEvent} からの登録はできなくなった
     * (AE2 の {@code InitScreens#register} もイベントを第 1 引数に取るようになっている)。
     */
    private static void registerScreens(RegisterMenuScreensEvent event) {
        InitScreens.register(event, OverdriveInscriberMenu.TYPE, OverdriveInscriberScreen::new,
                "/screens/insaneae/quantum_cpu.json");
        /** そのままだと型推論がうまくいかないのでクラス指定しておく */
        InitScreens.register(event, OverdriveMachineMenu.TYPE, OverdriveMachineScreen<OverdriveMachineMenu>::new ,
                "/screens/insaneae/quantum_cpu.json");
    }
}
