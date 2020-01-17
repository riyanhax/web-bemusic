import {User} from "vebto-client/core/types/models/User";

export class GenresUser {
	id: number;
	name: string;
	created_at: string;
	updated_at: string;
	user?: User;

	constructor(params: Object = {}) {
        for (let name in params) {
            this[name] = params[name];
        }
    }
}