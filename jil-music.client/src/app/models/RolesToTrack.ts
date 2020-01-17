

export class RolesToTrack {
	id: number;
	name: string;
	track_id?: number;	
	roles?: string[];	
	updated_at?: string;
	
	constructor(params: Object = {}) {
        for (let name in params) {
            this[name] = params[name];
        }
    }
}