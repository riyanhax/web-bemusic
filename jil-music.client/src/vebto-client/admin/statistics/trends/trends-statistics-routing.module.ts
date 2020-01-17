import {Route} from '@angular/router';

import {OverviewTrendsStatisticsComponent} from "./overview/overview-statistics.component";
import {StandingsTrendsStatisticsComponent} from "./standings/standings-statistics.component";

export const vebtoTrendsStatisticsRoutes: Route[] = [
    // {path: '', redirectTo: 'overview', pathMatch: 'full'},
    {path: 'overview', component: OverviewTrendsStatisticsComponent, pathMatch: 'full'},        
    {path: 'standings', component: StandingsTrendsStatisticsComponent},    
];

// @NgModule({
//     imports: [RouterModule.forChild(routes)],
//     exports: [RouterModule]
// })
// export class SettingsRoutingModule {}