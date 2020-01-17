export class VideoAd {
	id: number;
	name: string;
	url: string;
	status: string;
	duration?: number;
	
	type?: string;	
	link?: string;
	text?: string;
	link_text?: string;
		
	constructor(params: Object = {}) {
        for (let name in params) {
            this[name] = params[name];
        }
    }
}