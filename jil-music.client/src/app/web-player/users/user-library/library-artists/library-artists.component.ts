import {Component, OnInit, ViewEncapsulation} from '@angular/core';
import {FilterablePage} from "../../../filterable-page/filterable-page";
import {Artist} from "../../../../models/Artist";
import {ActivatedRoute} from "@angular/router";
import {WebPlayerUrls} from "../../../web-player-urls.service";

@Component({
    selector: 'library-artists',
    templateUrl: './library-artists.component.html',
    styleUrls: ['./library-artists.component.scss'],
    encapsulation: ViewEncapsulation.None,
    host: {class: 'user-library-page'},
})
export class LibraryArtistsComponent extends FilterablePage<Artist> implements OnInit {

    public isMobile: boolean = false;
    /**
     * Current order for library albums.
     */
    public order = 'Date Added';

    /**
     * LibraryArtistsComponent Constructor.
     */
    constructor(private route: ActivatedRoute, public urls: WebPlayerUrls) {
        super(['name']);
        this.isMobile = window.matchMedia && window.matchMedia('(max-width: 768px)').matches;
    }

    ngOnInit() {
        this.route.data.subscribe(data => {
            this.setItems(data.artists);
        });
    }
}
