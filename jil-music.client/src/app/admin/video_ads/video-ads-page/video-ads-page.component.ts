import {Component, OnInit, ViewChild, ViewEncapsulation} from '@angular/core';
import {VideoAds} from "../../../web-player/video_ads/video_ads.service";
import {FormattedDuration} from "../../../web-player/player/formatted-duration.service";
import {ActivatedRoute, Router} from "@angular/router";
import {UrlAwarePaginator} from "vebto-client/admin/pagination/url-aware-paginator.service";
import {Modal} from "vebto-client/core/ui/modal.service";
import {CurrentUser} from "vebto-client/auth/current-user";
import {VideoAd} from '../../../models/VideoAd';
import {MatSort} from '@angular/material';
import {NewVideoadModalComponent} from '../new-videoad-modal/new-videoad-modal.component';
import {ConfirmModalComponent} from 'vebto-client/core/ui/confirm-modal/confirm-modal.component';
import {PaginatedDataTableSource} from 'vebto-client/admin/data-table/data/paginated-data-table-source';


@Component({
    selector: 'video-ads-page',
    templateUrl: './video-ads-page.component.html',
    styleUrls: ['./video-ads-page.component.scss'],
    providers: [UrlAwarePaginator],
    encapsulation: ViewEncapsulation.None
})
export class VideoAdsPageComponent implements OnInit {
    @ViewChild(MatSort) matSort: MatSort;

    public dataSource: PaginatedDataTableSource<VideoAd>;

    /**
     * TracksPageComponent Constructor.
     */
    constructor(
        private modal: Modal,
        private videoads: VideoAds,
        private duration: FormattedDuration,
        private route: ActivatedRoute,
        private router: Router,
        public currentUser: CurrentUser,
        private paginator: UrlAwarePaginator,
    ) {}

    ngOnInit() {
        this.crupdateVideoadBasedOnQueryParams();

        this.dataSource = new PaginatedDataTableSource<VideoAd>({
            uri: 'videoads',
            dataPaginator: this.paginator,
            matSort: this.matSort
        });
    }

    /**
     * Open modal for editing existing track or creating a new one.
     */
    public openCrupdateVideoadModal(videoad?: VideoAd) {
        this.dataSource.deselectAllItems();
        this.modal.open(NewVideoadModalComponent, {videoad}, 'new-videoad-modal-container')
            .afterClosed().subscribe(videoad => videoad && this.dataSource.refresh());
    }

    /**
     * Ask user to confirm deletion of selected tracks
     * and delete selected artists if user confirms.
     */
    public maybeDeleteSelectedVideoads() {
        this.modal.show(ConfirmModalComponent, {
            title: 'Delete Videoads',
            body:  'Are you sure you want to delete selected videoads?',
            ok:    'Delete'
        }).beforeClose().subscribe(confirmed => {
            if (confirmed) {
                this.deleteSelectedVideoads();
            } else {
                this.dataSource.deselectAllItems();
            }
        });
    }

    /**
     * Delete currently selected artists.
     */
    private deleteSelectedVideoads() {
        const ids = this.dataSource.selectedRows.selected.map(videoad => videoad.id);

        this.videoads.delete(ids).subscribe(() => {
            this.dataSource.refresh();
        });
    }

    /**
     * Ask user to confirm activate of selected ads
     * and activate selected ads if user confirms.
     */
    public maybeActivateSelectedAds() {
        this.modal.show(ConfirmModalComponent, {
            title: 'Activate Ads',
            body:  'Are you sure you want to activate selected ads?',
            ok:    'Activate'
        }).afterClosed().subscribe(confirmed => {
            if ( ! confirmed) return;
            this.activateSelectedAds();
        });
    }

    /**
     * Ask user to disable of selected ads
     * and disable selected ads if user confirms.
     */
    public maybeDisableSelectedAds() {
        this.modal.show(ConfirmModalComponent, {
            title: 'Disable Ads',
            body:  'Are you sure you want to disable selected ads?',
            ok:    'Disable'
        }).afterClosed().subscribe(confirmed => {
            if ( ! confirmed) return;
            this.disableSelectedAds();
        });
    }

    /**
     * Delete currently selected artists.
     */
    private activateSelectedAds() {
        const ids = this.dataSource.selectedRows.selected.map(videoad => videoad.id);

        this.videoads.activate(ids).subscribe(() => {
            this.dataSource.refresh();
        });
    }

    /**
     * Delete currently selected artists.
     */
    private disableSelectedAds() {
        const ids = this.dataSource.selectedRows.selected.map(videoad => videoad.id);

        this.videoads.disable(ids).subscribe(() => {
            this.dataSource.refresh();
        });
    }

    public formatDuration(duration: number) {
        return this.duration.fromMilliseconds(duration);
    }

    /**
     * Open crupdate Videoad modal if Videoad id is specified in query params.
     */
    private crupdateVideoadBasedOnQueryParams() {
        let videoadId = +this.route.snapshot.queryParamMap.get('id'),
            newVideoad = this.route.snapshot.queryParamMap.get('newVideoad');

        if ( ! videoadId && ! newVideoad) return;

        this.router.navigate([], {replaceUrl: true}).then(async () => {
            let videoad = videoadId ? await this.videoads.get(videoadId).toPromise() : null;

            this.openCrupdateVideoadModal(videoad);
        });
    }
}
