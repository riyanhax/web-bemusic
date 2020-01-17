import {Route} from '@angular/router';

import {TrendsStatisticsComponent} from "./trends/trends-statistics.component";
import {SharesStatisticsComponent} from "./shares/shares-statistics.component";
import {SalesStatisticsComponent} from "./sales/sales-statistics.component";

import {vebtoTrendsStatisticsRoutes} from './trends/trends-statistics-routing.module';

export const vebtoStatisticsRoutes: Route[] = [
    {path: '', redirectTo: 'trends', pathMatch: 'full'},
    {path: 'trends', component: TrendsStatisticsComponent, pathMatch: 'full',
        children: [                    
            ...vebtoTrendsStatisticsRoutes,
        ],
    },    
    {path: 'shares', component: SharesStatisticsComponent},
    {path: 'sales', component: SalesStatisticsComponent},      
];

// @NgModule({
//     imports: [RouterModule.forChild(routes)],
//     exports: [RouterModule]
// })
// export class SettingsRoutingModule {}