import {Routes} from '@angular/router';
import {GroupsComponent} from "./groups/groups.component";
import {UsersComponent} from "./users/users.component";
import {PagesComponent} from "./pages/pages.component";
import {CrupdatePageComponent} from "./pages/crupdate-page/crupdate-page.component";
import {TranslationsComponent} from "./translations/translations.component";
import {LocalizationsResolve} from "./translations/localizations-resolve.service";
import {MailTemplatesComponent} from "./mail-templates/mail-templates.component";
import {MailTemplatesResolve} from "./mail-templates/mail-templates-resolve.service";
import {AdsPageComponent} from "./ads-page/ads-page.component";
import {SubscriptionsListComponent} from './billing/subscriptions/subscriptions-list/subscriptions-list.component';
import {PlansListComponent} from './billing/plans/plans-list/plans-list.component';
import {BillingEnabledGuard} from '../billing/guards/billing-enabled-guard.service';
import {AnalyticsResolve} from './analytics/analytics-resolve.service';
import {AnalyticsComponent} from './analytics/analytics.component';
import {AuthGuard} from '../guards/auth-guard.service';

// import {StatisticsResolve} from './statistics/statistics-resolve.service';
// import {StatisticsComponent} from './statistics/statistics.component';
// import {TrendsStatisticsResolve} from './statistics/trends/trends-statistics-resolve.service';
// import {TrendsStatisticsComponent} from './statistics/trends/trends-statistics.component';
// import {SalesStatisticsResolve} from './statistics/sales/sales-statistics-resolve.service';
// import {SalesStatisticsComponent} from './statistics/sales/sales-statistics.component';
// import {SharesStatisticsResolve} from './statistics/shares/shares-statistics-resolve.service';
// import {SharesStatisticsComponent} from './statistics/shares/shares-statistics.component';

export const vebtoAdminRoutes: Routes = [
    {
        path: '',
        redirectTo: 'analytics',
        pathMatch: 'full',
    },
    // {
    //     path: 'statistics',
    //     component: StatisticsComponent,
    //     resolve: {stats: StatisticsResolve},
    //     canActivate: [AuthGuard],
    //     data: {permissions: ['reports.view']},        
    //     children: [
    //         {
    //             path: 'trends', 
    //             component: TrendsStatisticsComponent,
    //             resolve: {stats: TrendsStatisticsResolve},
    //             canActivate: [AuthGuard],
    //             data: {permissions: ['reports.view']},    
    //         },
    //         {
    //             path: 'sales',
    //             component: SalesStatisticsComponent,
    //             resolve: {stats: SalesStatisticsResolve},
    //             canActivate: [AuthGuard],
    //             data: {permissions: ['reports.view']},
    //         },    
    //         {
    //             path: 'shares',
    //             component: SharesStatisticsComponent,
    //             resolve: {stats: SharesStatisticsResolve},
    //             canActivate: [AuthGuard],
    //             data: {permissions: ['reports.view']},
    //         },
    //     ]
    // },    
    {
        path: 'analytics',
        component: AnalyticsComponent,
        resolve: {stats: AnalyticsResolve},
        canActivate: [AuthGuard],
        data: {permissions: ['reports.view']}
    },
    {
        path: 'users',
        component: UsersComponent,
        data: {permissions: ['users.view']}
    },
    {
        path: 'groups',
        component: GroupsComponent,
        data: {permissions: ['groups.view']}
    },
    {
        path: 'translations',
        component: TranslationsComponent,
        resolve: {localizations: LocalizationsResolve},
        data: {permissions: ['localizations.view']}
    },
    {
        path: 'mail-templates',
        component: MailTemplatesComponent,
        resolve: {templates: MailTemplatesResolve},
        data: {permissions: ['mail_templates.view']}
    },
    {
        path: 'pages',
        component: PagesComponent,
        data: {permissions: ['pages.view']}},
    {
        path: 'pages/new',
        component: CrupdatePageComponent,
        data: {permissions: ['pages.create']}
    },
    {
        path: 'pages/:id/edit',
        component: CrupdatePageComponent,
        data: {permissions: ['pages.update']}
    },
    {
        path: 'ads',
        component: AdsPageComponent,
        data: {permissions: ['ads.update']}
    },
    
    //billing
    {
        path: 'plans',
        component: PlansListComponent,
        canActivate: [BillingEnabledGuard],
        data: {permissions: ['plans.view']}
    },

    {
        path: 'subscriptions',
        component: SubscriptionsListComponent,
        canActivate: [BillingEnabledGuard],
        data: {permissions: ['subscriptions.view']}
    },
];

// @NgModule({
//     imports: [RouterModule.forChild(routes)],
//     exports: [RouterModule]
// })
// export class AdminRoutingModule {
// }