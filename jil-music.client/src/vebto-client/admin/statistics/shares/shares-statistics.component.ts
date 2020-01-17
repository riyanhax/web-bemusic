import {Component, ViewEncapsulation} from "@angular/core";
import {StatisticsPanelComponent} from "../statistics-panel.component";

@Component({
    selector: 'shares-statistics',
    templateUrl: './shares-statistics.component.html',
    encapsulation: ViewEncapsulation.None,
})
export class SharesStatisticsComponent extends StatisticsPanelComponent {
}
