import {Component, ViewEncapsulation} from "@angular/core";
import {StatisticsPanelComponent} from "../statistics-panel.component";

@Component({
    selector: 'sales-statistics',
    templateUrl: './sales-statistics.component.html',
    encapsulation: ViewEncapsulation.None,
})
export class SalesStatisticsComponent extends StatisticsPanelComponent {
}
