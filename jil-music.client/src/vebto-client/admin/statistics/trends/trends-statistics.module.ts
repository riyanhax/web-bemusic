import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TrendsStatisticsComponent} from "./trends-statistics.component";
import {TrendsStatisticsPanelComponent} from "./trends-statistics-panel.component";

import {OverviewTrendsStatisticsComponent} from "./overview/overview-statistics.component";
import {StandingsTrendsStatisticsComponent} from "./standings/standings-statistics.component";


import {TrendsStatisticsResolve} from "./trends-statistics-resolve.service";

import {RouterModule} from "@angular/router";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";

import {
    MatAutocompleteModule,
    MatButtonModule, MatCheckboxModule,
    MatDialogModule, MatFormFieldModule, MatInputModule,
    MatMenuModule, MatPaginatorModule, MatSlideToggleModule, MatSnackBarModule, MatSortModule, MatTableModule,
    MatTooltipModule, MatChipsModule
} from "@angular/material";
import {UiModule} from "../../../core/ui/ui.module";

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
        TrendsStatisticsComponent,
        TrendsStatisticsPanelComponent,

        OverviewTrendsStatisticsComponent,
        StandingsTrendsStatisticsComponent,
    ],
    providers: [
        TrendsStatisticsResolve,
    ]
})
export class StatisticsModule {
}
