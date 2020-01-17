import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {StatisticsComponent} from "./statistics.component";
import {StatisticsPanelComponent} from "./statistics-panel.component";

// import {TrendsStatisticsComponent} from "./trends/trends-statistics.component";
import {SharesStatisticsComponent} from "./shares/shares-statistics.component";
import {SalesStatisticsComponent} from "./sales/sales-statistics.component";

import {StatisticsResolve} from "./statistics-resolve.service";
import {StatisticsState} from "./statistics-state.service";
import {RouterModule} from "@angular/router";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";

import {
    MatAutocompleteModule,
    MatButtonModule, MatCheckboxModule,
    MatDialogModule, MatFormFieldModule, MatInputModule,
    MatMenuModule, MatPaginatorModule, MatSlideToggleModule, MatSnackBarModule, MatSortModule, MatTableModule,
    MatTooltipModule, MatChipsModule
} from "@angular/material";
import {UiModule} from "../../core/ui/ui.module";

@NgModule({
    imports: [
        RouterModule,
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        UiModule,

        //material
        MatButtonModule,
        MatSnackBarModule,
        MatTableModule,
        MatCheckboxModule,
        MatPaginatorModule,
        MatSortModule,
        MatTooltipModule,
        MatDialogModule,
        MatMenuModule,
        MatSlideToggleModule,
        MatFormFieldModule,
        MatAutocompleteModule,
        MatInputModule,
        MatChipsModule,
    ],
    declarations: [
        StatisticsComponent,
        StatisticsPanelComponent,        
        SharesStatisticsComponent,
        SalesStatisticsComponent,        

        // TrendsStatisticsComponent,        
    ],
    providers: [
        StatisticsResolve,
        StatisticsState,
    ]
})
export class StatisticsModule {
}
