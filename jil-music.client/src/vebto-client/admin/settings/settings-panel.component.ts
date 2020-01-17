import {Component, ViewEncapsulation} from "@angular/core";
import {SettingsState} from "./settings-state.service";
import {ActivatedRoute} from "@angular/router";
import {finalize} from "rxjs/operators";
import {Settings} from "../../core/config/settings.service";
import {Toast} from "../../core/ui/toast.service";
import {AppHttpClient} from "vebto-client/core/http/app-http-client.service";
import {Modal} from "../../core/ui/modal.service";
import {Pages} from '../../core/pages/pages.service';
import {CustomHomepage} from '../../core/pages/custom-homepage.service';

import {Search} from '../../../app/web-player/search/search.service';
import {Albums} from "../../../app/web-player/albums/albums.service";
import {Tracks} from "../../../app/web-player/tracks/tracks.service";
import {Genres} from "../../../app/web-player/genres/genres.service";

@Component({
    selector: 'settings-panel',
    template: '',
    encapsulation: ViewEncapsulation.None,
})
export class SettingsPanelComponent {

    public loading = false;

    constructor(
        public settings: Settings,
        protected toast: Toast,
        protected http: AppHttpClient,
        protected modal: Modal,
        protected route: ActivatedRoute,
        protected pages: Pages,
        protected customHomepage: CustomHomepage,
        public state: SettingsState,
        protected search: Search,
        protected albums: Albums,
        protected tracks: Tracks,
        protected genres: Genres,
    ) {}

    /**
     * Save current settings to the server.
     */
    public saveSettings(settings?: object) {
        this.loading = true;

        this.settings.save(settings || this.state.getModified())
            .pipe(finalize(() => this.loading = false))
            .subscribe(() => {
                this.toast.open('Saved settings');
            });
    }
}
