package io.openems.edge.ess.generic.common.offgrid.statemachine;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.edge.common.startstop.StartStop;
import io.openems.edge.common.statemachine.StateHandler;
import io.openems.edge.common.sum.GridMode;
import io.openems.edge.ess.generic.common.GenericManagedEss;
import io.openems.edge.ess.generic.common.offgrid.statemachine.OffGridStateMachine.OffGridState;

public class StartedInOffGridHandler extends StateHandler<OffGridState, OffGridContext> {

	@Override
	public OffGridState runAndGetNextState(OffGridContext context) throws OpenemsNamedException {
		GenericManagedEss ess = context.getParent();

		if (ess.hasFaults()) {
			return OffGridState.UNDEFINED;
		}

		if (!context.battery.isStarted()) {
			return OffGridState.UNDEFINED;
		}

		if (!context.batteryInverter.isStarted()) {
			return OffGridState.UNDEFINED;
		}

		// Grid is On?
		if (!context.offGridSwitch.getGridStatus()) {
			return OffGridState.STOP_BATTERY_INVERTER_BEFORE_SWITCH;
		}

		// Mark as started
		ess._setStartStop(StartStop.START);
		context.batteryInverter.setOffgridCommand();
		context.batteryInverter.setOffGridFrequency(52);
		context.getParent()._setGridMode(GridMode.OFF_GRID);
		return OffGridState.STARTED_IN_OFF_GRID;
	}

}
