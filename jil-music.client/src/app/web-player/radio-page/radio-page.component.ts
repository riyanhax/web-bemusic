import {Component, OnInit, ViewEncapsulation, HostListener, ViewChild, ElementRef} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {Track} from "../../models/Track";
import {Artist} from "../../models/Artist";
import {Player} from "../player/player.service";
import {Translations} from "vebto-client/core/translations/translations.service";

import { Location } from '@angular/common';

@Component({
    selector: 'radio-page',
    templateUrl: './radio-page.component.html',
    styleUrls: ['./radio-page.component.scss'],
    encapsulation: ViewEncapsulation.None
})
export class RadioPageComponent implements OnInit {

    @ViewChild('scrollContainer') private scrollContainer: ElementRef;
    @ViewChild('scrollImgSmall') private scrollImgSmall: ElementRef;
    public scrollContainerScrolled:boolean = false;
    public scrollHeaderScrolled:boolean = false;
    @HostListener('scroll', ['$event'])
        onScroll(event) {         
            if(this.isMobile){   
                const scrollPosition = this.scrollContainer.nativeElement.scrollTop;
                if(scrollPosition < 250){                                
                    this.scrollImgSmall.nativeElement.style.height = (150-scrollPosition/1.8)+'px';
                    this.scrollImgSmall.nativeElement.style.width = (150-scrollPosition/1.8) +'px';
                }

                if(scrollPosition > 130 && !this.scrollHeaderScrolled){
                    this.scrollHeaderScrolled = true;
                    this.scrollContainer.nativeElement.classList.add('header-static');                
                    // this.scrollContainer.nativeElement.classList.add('btn-play-static');                
                }else if(scrollPosition < 130 && this.scrollHeaderScrolled){
                    this.scrollHeaderScrolled = false;
                    this.scrollContainer.nativeElement.classList.remove('header-static');
                    // this.scrollContainer.nativeElement.classList.remove('btn-play-static');
                }

                if(scrollPosition > 200 && !this.scrollContainerScrolled){
                    this.scrollContainerScrolled = true;
                    this.scrollContainer.nativeElement.classList.add('btn-play-static');                
                }else if(scrollPosition < 200 && this.scrollContainerScrolled){
                    this.scrollContainerScrolled = false;
                    this.scrollContainer.nativeElement.classList.remove('btn-play-static');
                }
            }
        }

    public isMobile: boolean = false;

    /**
     * Tracks recommended for seed radio.
     */
    public tracks: Track[];

    /**
     * Seed radio was generated for.
     */
    public seed: Artist|Track;

    /**
     * Type of radio seed.
     */
    public type: string;

    /**
     * RadioPageComponent Constructor.
     */
    constructor(
        private route: ActivatedRoute,
        private player: Player,
        private i18n: Translations,
        private location: Location,

        protected el: ElementRef,
    ) {
        this.isMobile = window.matchMedia && window.matchMedia('(max-width: 768px)').matches;
    }

    ngOnInit() {
        this.route.data.subscribe(data => {
            this.seed = data.radio.seed;
            this.type = this.i18n.t(data.radio.type);

            this.tracks = data.radio.recommendations.map(track => {
                return new Track(track);
            });
        });
    }

    goBack() {
        this.location.back(); // go back to previous location
    }
    
    /**
     * Get image for this radio.
     */
    public getImage(): string {
        return this.seed['image_small'] || this.seed['album'].image;
    }

    /**
     * Check if this radio is playing currently.
     */
    public playing(): boolean {
        return this.player.isPlaying() && this.isQueued();
    }

    /**
     * Play the radio.
     */
    public async play() {
        if ( ! this.isQueued()) {
            await this.player.overrideQueue({tracks: this.tracks, queuedItemId: this.getQueueId()});
        }

        this.player.play();
    }

    /**
     * Pause the player.
     */
    public pause() {
        this.player.pause();
    }

    /**
     * Get queue ID for this radio.
     */
    private getQueueId(): string {
        return 'radio.'+this.seed.id
    }

    /**
     * Check if this radio is currently queued in player.
     */
    private isQueued(): boolean {
        return this.player.queue.itemIsQueued(this.getQueueId());
    }

}
