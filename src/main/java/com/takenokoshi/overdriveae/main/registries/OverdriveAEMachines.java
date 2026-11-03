package com.takenokoshi.overdriveae.main.registries;

import com.takenokoshi.overdriveae.main.OverdriveAE;
import com.takenokoshi.overdriveae.main.block.machine.OverdriveMachineBlock;
import com.takenokoshi.overdriveae.main.block.machine.OverdriveMachineOldBlock;
import com.takenokoshi.overdriveae.main.blockentity.machine.OverdriveChargerBlockEntity;
import com.takenokoshi.overdriveae.main.blockentity.machine.OverdriveCondenserBlockEntity;
import com.takenokoshi.overdriveae.main.blockentity.machine.OverdriveEnergizedBlastFurnaceBlockEntity;
import com.takenokoshi.overdriveae.main.blockentity.machine.OverdriveEnergizedFurnaceBlockEntity;
import com.takenokoshi.overdriveae.main.blockentity.machine.OverdriveEnergizedSmokerBlockEntity;
import com.takenokoshi.overdriveae.main.blockentity.machine.OverdriveInscriberBlockEntity;
import com.takenokoshi.overdriveae.main.blockentity.machine.OverdriveTransformChamberBlockEntity;
import com.takenokoshi.overdriveae.main.registration.OverdriveAEDeferredMachine;
import com.takenokoshi.overdriveae.main.registration.OverdriveAEMachineDeferredRegister;

public class OverdriveAEMachines {
    public static final OverdriveAEMachineDeferredRegister MACHINES = new OverdriveAEMachineDeferredRegister(
            OverdriveAE.MODID);

    public static final OverdriveAEDeferredMachine<OverdriveMachineOldBlock<OverdriveChargerBlockEntity>, OverdriveChargerBlockEntity> OVERDRIVE_CHARGER = MACHINES
            .register("overdrive_charger", OverdriveMachineOldBlock::new, OverdriveChargerBlockEntity.class,
                    OverdriveChargerBlockEntity::new, null, (level, pos, state, be) -> be.serverTick());

    public static final OverdriveAEDeferredMachine<OverdriveMachineOldBlock<OverdriveInscriberBlockEntity>, OverdriveInscriberBlockEntity> OVERDRIVE_INSCRIBER = MACHINES
            .register("overdrive_inscriber", OverdriveMachineOldBlock::new, OverdriveInscriberBlockEntity.class,
                    OverdriveInscriberBlockEntity::new, null, (level, pos, state, be) -> be.serverTick());

    public static final OverdriveAEDeferredMachine<OverdriveMachineOldBlock<OverdriveCondenserBlockEntity>, OverdriveCondenserBlockEntity> OVERDRIVE_MATTER_CONDENSER = MACHINES
            .register("overdrive_matter_condenser", OverdriveMachineOldBlock::new, OverdriveCondenserBlockEntity.class,
                    OverdriveCondenserBlockEntity::new, null, (level, pos, state, be) -> be.serverTick());

    public static final OverdriveAEDeferredMachine<OverdriveMachineOldBlock<OverdriveTransformChamberBlockEntity>, OverdriveTransformChamberBlockEntity> OVERDRIVE_TRANSFORM_CHAMBER = MACHINES
            .register("overdrive_transform_chamber", OverdriveMachineOldBlock::new,
                    OverdriveTransformChamberBlockEntity.class, OverdriveTransformChamberBlockEntity::new, null,
                    (level, pos, state, be) -> be.serverTick());

    public static final OverdriveAEDeferredMachine<OverdriveMachineBlock<OverdriveEnergizedFurnaceBlockEntity>, OverdriveEnergizedFurnaceBlockEntity> OVERDRIVE_ENERGIZED_FURNACE = MACHINES
            .register("overdrive_energized_furnace", OverdriveMachineBlock::new,
                    OverdriveEnergizedFurnaceBlockEntity.class,
                    OverdriveEnergizedFurnaceBlockEntity::new, null, (level, pos, state, be) -> be.serverTick());

    public static final OverdriveAEDeferredMachine<OverdriveMachineOldBlock<OverdriveEnergizedBlastFurnaceBlockEntity>, OverdriveEnergizedBlastFurnaceBlockEntity> OVERDRIVE_ENERGIZED_BLAST_FURNACE = MACHINES
            .register("overdrive_energized_blast_furnace", OverdriveMachineOldBlock::new,
                    OverdriveEnergizedBlastFurnaceBlockEntity.class,
                    OverdriveEnergizedBlastFurnaceBlockEntity::new, null, (level, pos, state, be) -> be.serverTick());

    public static final OverdriveAEDeferredMachine<OverdriveMachineOldBlock<OverdriveEnergizedSmokerBlockEntity>, OverdriveEnergizedSmokerBlockEntity> OVERDRIVE_ENERGIZED_SMOKER = MACHINES
            .register("overdrive_energized_smoker", OverdriveMachineOldBlock::new,
                    OverdriveEnergizedSmokerBlockEntity.class,
                    OverdriveEnergizedSmokerBlockEntity::new, null, (level, pos, state, be) -> be.serverTick());
}
