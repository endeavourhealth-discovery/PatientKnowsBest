//This will store the current logged in user and validates the user

export class User {

  private jwtToken: string;
  private userDetails: { sub: string, exp: number, iat: number };


  constructor(jwtToken: string) {
    this.jwtToken = jwtToken;
    this.userDetails = this.parseJwt(jwtToken);
  }

  private parseJwt(token) {
    var base64Url = token.split('.')[1];
    var base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    var jsonPayload = decodeURIComponent(atob(base64).split('').map(function (c) {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));

    return JSON.parse(jsonPayload);
  };

  get token(): string {
    //  check if the token is valid if valid send the token else return null

    if (this.userDetails.exp == null || new Date() > new Date(this.userDetails.exp * 1000)) {

      return null;

    }

    return this.jwtToken;
  }

  get expiry(): number {
    if (this.userDetails.exp == null || new Date() > new Date(this.userDetails.exp * 1000)) {

      return null;
    } else {
      return (new Date(this.userDetails.exp * 1000).getTime() - new Date().getTime())
    }

  }

  get userName():string{
  return   this.userDetails.sub;
}



}
