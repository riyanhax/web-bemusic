import {Component, ViewEncapsulation, NgZone, AfterViewInit, ElementRef} from '@angular/core';
import {Player} from "../player.service";
import {FullscreenOverlay} from "../../fullscreen-overlay/fullscreen-overlay.service";

@Component({
    selector: 'mobile-player-controls',
    templateUrl: './mobile-player-controls.component.html',
    styleUrls: ['./mobile-player-controls.component.scss'],
    encapsulation: ViewEncapsulation.None
})
export class MobilePlayerControlsComponent implements AfterViewInit {

    public isMobile: boolean = false;

    constructor(
        public player: Player,
        public overlay: FullscreenOverlay,

        private ngZone: NgZone,   
        private el: ElementRef,
    ) {
        this.isMobile = window.matchMedia && window.matchMedia('(max-width: 768px)').matches;
    }
   
 /**
     * Called after component's view has been fully initialized.
     */
    ngAfterViewInit() {

        //wait for animations to complete
        //TODO: refactor this to use events instead
        setTimeout(() => {            
            this.bindHammerEvents();                        
        }, 1501);
    }
/**
     * Bind handlers to needed hammer.js events.
     */
    private bindHammerEvents() {
        let hammer, tap, pan;

        this.ngZone.runOutsideAngular(() => {            
            hammer = new Hammer.Manager(this.el.nativeElement);
            tap = new Hammer.Tap();
            pan = new Hammer.Pan();
            hammer.add([tap, pan]);            
        });

        hammer.on("panup pandown", e => {
            this.ngZone.run(() => {
                if(e.type == 'panup' && !this.overlay.isMaximized()) {
                    this.overlay.maximize();                                    
                }
                if(e.type == 'pandown' && this.overlay.isMaximized()){
                    this.overlay.minimize();
                }            
            });
            
        });
    }

}
