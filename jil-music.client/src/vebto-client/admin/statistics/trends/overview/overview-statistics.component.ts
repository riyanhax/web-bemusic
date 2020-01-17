import {Component, OnInit, ViewEncapsulation} from "@angular/core";
import {TrendsStatisticsComponent} from "../trends-statistics.component";


@Component({
    selector: 'overview-statistics',
    templateUrl: './overview-statistics.component.html',
    encapsulation: ViewEncapsulation.None,
})
export class OverviewTrendsStatisticsComponent extends TrendsStatisticsComponent {
}
