import {Component, OnInit, ViewEncapsulation} from "@angular/core";
import {SettingsPanelComponent} from 'vebto-client/admin/settings/settings-panel.component';

@Component({
    selector: 'roles-artists-settings',
    templateUrl: './roles-artists-settings.component.html',
    styleUrls: ['./roles-artists-settings.component.scss'],
    encapsulation: ViewEncapsulation.None,
})
export class RolesArtistsSettingsComponent extends SettingsPanelComponent implements OnInit {

    /**
     * Blocked roles input model.
     */
    public rolesArtist: string;

    /**
     * List of roles artist names.
     */
    public rolesArtists: string[] = [];

    ngOnInit() {
        const rolesArtists = this.state.client['artists.roles'];
        this.rolesArtists = rolesArtists ? JSON.parse(rolesArtists) : [];
    }

    /**
     * Add a new artist role to roles artists list.
     */
    public addRolesArtist() {
        if ( ! this.rolesArtist) return;

        if (this.rolesArtists.findIndex(curr => curr === this.rolesArtist) > -1) {
            return this.rolesArtist = null;
        }

        this.rolesArtists.push(this.rolesArtist);
        this.rolesArtist = null;
    }

    /**
     * Remove specified artist role from roles artists list.
     */
    public removeRolesArtist(rolesArtist: string) {
        let i = this.rolesArtists.findIndex(curr => curr === rolesArtist);
        this.rolesArtists.splice(i, 1);
    }

    /**
     * Save current settings to the server.
     */
    public saveSettings() {
        let payload = {'artists.roles': JSON.stringify(this.rolesArtists)};
        this.loading = true;

        this.settings.save({client: payload}).subscribe(() => {
            this.toast.open('Saved settings');
            this.loading = false;
        });
    }
}
