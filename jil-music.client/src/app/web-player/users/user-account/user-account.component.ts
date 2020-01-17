import {Component, ViewEncapsulation} from '@angular/core';
import {WebPlayerState} from "../../web-player-state.service";

@Component({
    selector: 'user-account',
    templateUrl: './user-account.component.html',
    styleUrls: ['./user-account.component.scss'],
    encapsulation: ViewEncapsulation.None
})
export class UserAccountComponent {

    constructor(
        public state: WebPlayerState
    ) {
        
    }
}
