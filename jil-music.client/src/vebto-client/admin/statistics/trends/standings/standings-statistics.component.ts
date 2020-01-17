import {Component, OnInit, ViewEncapsulation} from "@angular/core";
import {TrendsStatisticsComponent} from "../trends-statistics.component";

@Component({
    selector: 'standings-statistics',
    templateUrl: './standings-statistics.component.html',
    encapsulation: ViewEncapsulation.None,
})
export class StandingsTrendsStatisticsComponent extends TrendsStatisticsComponent {
}
