import {Component, ViewEncapsulation, OnInit} from "@angular/core";
import {SettingsPanelComponent} from "../settings-panel.component";

@Component({
    selector: 'videoads-settings',
    templateUrl: './videoads-settings.component.html',
    encapsulation: ViewEncapsulation.None,
})
export class VideoadsSettingsComponent extends SettingsPanelComponent implements OnInit {

    public acceptedTypes: string[] = [];

    public avalibleTypes = {
        video : false,
        promo : false,
        custom : false,
        adscene : false,
        adsima : false,
    };

    ngOnInit() {        
        this.acceptedTypes =  this.settings.has('videoads_source') ? JSON.parse(this.settings.get('videoads_source')) : [];
        this.CheckStateTypes();
    }
    
    public CheckStateTypes() {        
        this.acceptedTypes.map(key => {                                                
            this.avalibleTypes[key] = true;
        });        
    }

    /**
     * Save current settings to the server.
     */
    public saveSettings() {
        const settings = this.state.getModified();
        let newAcceptedTypes = [];

        for(var type in this.avalibleTypes) { 
            if(this.avalibleTypes[type]) newAcceptedTypes.push(type);            
        }
        settings.client['videoads_source'] = JSON.stringify(newAcceptedTypes);
        super.saveSettings(settings);

        this.http.post('cache/clear').subscribe(() => {
            console.log('Cache cleared.');
        });
    }

}
