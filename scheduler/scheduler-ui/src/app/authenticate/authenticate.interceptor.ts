import {HttpEvent, HttpHandler, HttpHeaders, HttpInterceptor, HttpParams, HttpRequest} from "@angular/common/http";
import {Observable} from "rxjs";
import {Injectable} from "@angular/core";
import {SchedulerService} from "../scheduler/scheduler.service";
import {exhaustMap, take} from "rxjs/operators";
import {User} from "./authenticate.model";
import {environment} from "../../environments/environment";



@Injectable()
export class interceptor implements HttpInterceptor {

  user: User;
  // url : string = 'http://localhost:7080/scheduler/';


  constructor(private _service: SchedulerService) {
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

    // Take operater will automatically unsubsribes once we get the observable
    this._service.user.pipe(take(1)).subscribe(user => {
      this.user = user;
    });
    if (this.user) {


      const modifiedreq = req.clone({params: req.params.set("Authorization", this.user.token),url:environment.serverUrl+req.url});

      return next.handle(modifiedreq);
    } else {

      const modifiedreq = req.clone({url:environment.serverUrl+req.url})
      return next.handle(modifiedreq);
    }


  }

}
