
import { filter } from 'rxjs/operators';
import { Injectable } from '@angular/core';
import { NavigationStart, Router } from "@angular/router";
import { Subscription, Observable } from "rxjs";

import {WebPlayerState} from "../web-player-state.service";
import {OverlayContainer} from '@angular/cdk/overlay';

@Injectable()
export class FullscreenAds {

    /**
     * Whether fullscreen overlay is currently maximized.
     */
    public maximized = false;
    public maximizedHeight = false;

    private adsType: 'promo' | 'video' | 'custom' | 'adscene' | 'adsima' = 'video';

    /**
     * Currently active fullscreen overlay panel.
     */
    private activePanel: 'queue' | 'video' = 'video';

    /**
     * Active service subscriptions.
     */
    protected subscriptions: Subscription[] = [];

    /**
     * FullscreenAds Constructor.
     */
    constructor(
        private router: Router,
        private state: WebPlayerState,
        private overlayContainer: OverlayContainer,
    ) { }

    /**
     * Init fullscreen overlay service.
     */
    public init() {        
        this.bindToRouterEvents();
    }

    public isType(type?: string) {                
        if(!type) return this.adsType;

        return type == this.adsType ? true : false;
    }    

    public setType(type: any) {                
        this.adsType = type;
    }

    /**
     * Check if fullscreen overlay is currently maximized.
     */
    public isMaximized(): boolean {
        return this.maximized;
    }

    public isMaximizedHeight(): boolean {
        return this.maximizedHeight;
    }
    

    /**
     * Minimize fullscreen overlay.
     */
    public minimize() {        
        if(this.state.isModalOpen){            
            this.overlayContainer.getContainerElement().classList.remove('home-modal-hide');
        }        
        this.maximizedHeight = false;           
        new Promise(resolve => {
            setTimeout(() => {                 
                this.maximized = false;                  
                this.openVideoPanel();                
             }, 301);
        });
    }

    /**
     * Maximize fullscreen overlay.
     */
    public maximize(): Promise<boolean> {             
        this.maximized = true;    
        this.maximizedHeight = true;     
        if(this.state.isModalOpen){            
            this.overlayContainer.getContainerElement().classList.add('home-modal-hide');
        }        
        //wait for animation to complete
        return new Promise(resolve => {
            setTimeout(() => { resolve() }, 201);
        });
    }

    public openQueuePanel() {
        this.activePanel = 'queue';
    }

    public openVideoPanel() {
        this.activePanel = 'video';
    }

    public activePanelIs(name: string) {
        return this.activePanel === name;
    }

    /**
     * Destroy fullscreen overlay service.
     */
    public destroy() {
        this.subscriptions.forEach(subscription => {
            subscription.unsubscribe();
        });
    }

    /**
     * Minimize fullscreen overlay when navigation occurs.
     */
    private bindToRouterEvents() {
        const sub = this.router.events.pipe(
            filter(e => e instanceof NavigationStart))
            .subscribe(() => this.minimize());

        this.subscriptions.push(sub);
    }

}
