package com.takenokoshi.overdriveae.registries;

import com.takenokoshi.overdriveae.OverdriveAE;
import com.takenokoshi.overdriveae.block.machine.OverdriveChargerBlock;
import com.takenokoshi.overdriveae.blockentity.machine.OverdriveChargerBlockEntity;
import com.takenokoshi.overdriveae.registration.OAEDeferredMachine;
import com.takenokoshi.overdriveae.registration.OAEMachineDeferredRegister;

public class OAEMachines {
    public static final OAEMachineDeferredRegister MACHINES = new OAEMachineDeferredRegister(OverdriveAE.MODID);

    public static final OAEDeferredMachine<OverdriveChargerBlock, OverdriveChargerBlockEntity> OVERDRIVE_CHARGER = MACHINES
            .register("overdrive_charger", OverdriveChargerBlock::new, OverdriveChargerBlockEntity.class,
                    OverdriveChargerBlockEntity::new, null, (level, pos, state, be) -> be.serverTick());
}
