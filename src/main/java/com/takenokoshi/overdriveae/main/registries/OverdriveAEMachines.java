package com.takenokoshi.overdriveae.main.registries;

import com.takenokoshi.overdriveae.main.OverdriveAE;
import com.takenokoshi.overdriveae.main.block.machine.OverdriveMachineBlock;
import com.takenokoshi.overdriveae.main.blockentity.machine.OverdriveChargerBlockEntity;
import com.takenokoshi.overdriveae.main.blockentity.machine.OverdriveCondenserBlockEntity;
import com.takenokoshi.overdriveae.main.blockentity.machine.OverdriveEnergizedBlastFurnaceBlockEntity;
import com.takenokoshi.overdriveae.main.blockentity.machine.OverdriveEnergizedFurnaceBlockEntity;
import com.takenokoshi.overdriveae.main.blockentity.machine.OverdriveEnergizedSmokerBlockEntity;
import com.takenokoshi.overdriveae.main.blockentity.machine.OverdriveInscriberBlockEntity;
import com.takenokoshi.overdriveae.main.registration.OverdriveAEDeferredMachine;
import com.takenokoshi.overdriveae.main.registration.OverdriveAEMachineDeferredRegister;

public class OverdriveAEMachines {
    public static final OverdriveAEMachineDeferredRegister MACHINES = new OverdriveAEMachineDeferredRegister(
            OverdriveAE.MODID);

    public static final OverdriveAEDeferredMachine<OverdriveMachineBlock<OverdriveChargerBlockEntity>, OverdriveChargerBlockEntity> OVERDRIVE_CHARGER = MACHINES
            .register("overdrive_charger", OverdriveMachineBlock::new, OverdriveChargerBlockEntity.class,
                    OverdriveChargerBlockEntity::new, null, (level, pos, state, be) -> be.serverTick());

    public static final OverdriveAEDeferredMachine<OverdriveMachineBlock<OverdriveInscriberBlockEntity>, OverdriveInscriberBlockEntity> OVERDRIVE_INSCRIBER = MACHINES
            .register("overdrive_inscriber", OverdriveMachineBlock::new, OverdriveInscriberBlockEntity.class,
                    OverdriveInscriberBlockEntity::new, null, (level, pos, state, be) -> be.serverTick());

    public static final OverdriveAEDeferredMachine<OverdriveMachineBlock<OverdriveCondenserBlockEntity>, OverdriveCondenserBlockEntity> OVERDRIVE_MATTER_CONDENSER = MACHINES
            .register("overdrive_matter_condenser", OverdriveMachineBlock::new, OverdriveCondenserBlockEntity.class,
                    OverdriveCondenserBlockEntity::new, null, (level, pos, state, be) -> be.serverTick());

    public static final OverdriveAEDeferredMachine<OverdriveMachineBlock<OverdriveEnergizedFurnaceBlockEntity>, OverdriveEnergizedFurnaceBlockEntity> OVERDRIVE_ENERGIZED_FURNACE = MACHINES
            .register("overdrive_energized_furnace", OverdriveMachineBlock::new,
                    OverdriveEnergizedFurnaceBlockEntity.class,
                    OverdriveEnergizedFurnaceBlockEntity::new, null, (level, pos, state, be) -> be.serverTick());

    public static final OverdriveAEDeferredMachine<OverdriveMachineBlock<OverdriveEnergizedBlastFurnaceBlockEntity>, OverdriveEnergizedBlastFurnaceBlockEntity> OVERDRIVE_ENERGIZED_BLAST_FURNACE = MACHINES
            .register("overdrive_energized_blast_furnace", OverdriveMachineBlock::new,
                    OverdriveEnergizedBlastFurnaceBlockEntity.class,
                    OverdriveEnergizedBlastFurnaceBlockEntity::new, null, (level, pos, state, be) -> be.serverTick());

    public static final OverdriveAEDeferredMachine<OverdriveMachineBlock<OverdriveEnergizedSmokerBlockEntity>, OverdriveEnergizedSmokerBlockEntity> OVERDRIVE_ENERGIZED_SMOKER = MACHINES
            .register("overdrive_energized_smoker", OverdriveMachineBlock::new,
                    OverdriveEnergizedSmokerBlockEntity.class,
                    OverdriveEnergizedSmokerBlockEntity::new, null, (level, pos, state, be) -> be.serverTick());
}
