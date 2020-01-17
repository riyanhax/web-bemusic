import {Component, ViewEncapsulation} from "@angular/core";
import {SettingsPanelComponent} from "../settings-panel.component";

@Component({
    selector: 'twilio-settings',
    templateUrl: './twilio-settings.component.html',
    encapsulation: ViewEncapsulation.None,
})
export class TwilioSettingsComponent extends SettingsPanelComponent {
}
