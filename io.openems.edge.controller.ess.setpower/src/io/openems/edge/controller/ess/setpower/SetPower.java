package io.openems.edge.controller.ess.setpower;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.types.ChannelAddress;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.IntegerReadChannel;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.ess.api.ManagedSymmetricEss;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "Controller.io.openems.edge.controller.ess.setpower", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
public class SetPower extends AbstractOpenemsComponent implements Controller, OpenemsComponent {

	@Reference
	protected ComponentManager componentManager;

	private Config config = null;

	public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
		;

		private final Doc doc;

		private ChannelId(Doc doc) {
			this.doc = doc;
		}

		@Override
		public Doc doc() {
			return this.doc;
		}
	}

	public SetPower() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				Controller.ChannelId.values(), //
				ChannelId.values() //
		);
	}

	@Activate
	void activate(ComponentContext context, Config config) throws OpenemsNamedException {
		this.config = config;
		super.activate(context, config.id(), config.alias(), config.enabled());
	}

	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	@Override
	public void run() throws OpenemsNamedException {	
		
		ManagedSymmetricEss ess = this.componentManager.getComponent(this.config.ess_id());

        int requiredPower = 0;
        for (String channelAdress : config.inputChannelAddress()) {
            String signum = channelAdress.substring(0, 1);
            channelAdress = channelAdress.substring(1, channelAdress.length());
            
            ChannelAddress inputChannelAddress = ChannelAddress.fromString(channelAdress);
            IntegerReadChannel inputChannel = this.componentManager.getChannel(inputChannelAddress);
            int value = inputChannel.value().getOrError();
            
            if ("+".equals(signum)) {
                requiredPower = requiredPower + value;
                System.out.print("+ ");
                
            } else if ("-".equals(signum)) {
                requiredPower = requiredPower - value;
                System.out.print("- ");
            }
            System.out.println(channelAdress + ": " + value);
        }
        
        System.out.println("Required power: "+ requiredPower);

		if (config.use_pid()) {
			ess.getSetActivePowerEqualsWithPid().setNextWriteValue(requiredPower);
		} else {
			ess.getSetActivePowerEquals().setNextWriteValue(requiredPower);
		}
	}
}
