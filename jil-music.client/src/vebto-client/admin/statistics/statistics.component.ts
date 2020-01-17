import {Component, OnInit, ViewEncapsulation} from "@angular/core";
import {ActivatedRoute} from "@angular/router";
import {StatisticsState} from "./statistics-state.service";
import {Settings} from "../../core/config/settings.service";

@Component({
    selector: 'statistics',
    templateUrl: './statistics.component.html',
    styleUrls: ['./statistics.component.scss'],
    encapsulation: ViewEncapsulation.None,
})
export class StatisticsComponent implements OnInit {

    constructor(
        public settings: Settings,
        private route: ActivatedRoute,
        private state: StatisticsState,
    ) {}

    ngOnInit() {
        this.route.data.subscribe(data => {
            this.state.setAll(data['settings']);
        });       

        console.log('==>');
        console.log(this.route.toString());
        console.log('<==');        
    }
}
