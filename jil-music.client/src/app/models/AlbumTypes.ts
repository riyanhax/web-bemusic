export class AlbumTypes {
    live?: boolean;
    mix?: boolean;
    comp?: boolean;

    constructor(params: Object = {}) {
        for (let name in params) {
            this[name] = params[name];
        }
    }
}