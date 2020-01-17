import {Component, OnInit, ViewEncapsulation} from "@angular/core";
import {ActivatedRoute} from "@angular/router";

@Component({
    selector: 'trends-statistics',
    templateUrl: './trends-statistics.component.html',    
    styleUrls: ['./trends-statistics.component.scss'],
    encapsulation: ViewEncapsulation.None,
})
export class TrendsStatisticsComponent implements OnInit {

    constructor(        
        private route: ActivatedRoute,
    ) {
        console.log(route);
    }

    ngOnInit() {
        
    }
}
