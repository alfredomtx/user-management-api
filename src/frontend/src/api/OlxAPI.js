import axios from "axios";
import Cookies from "js-cookie";

const http = axios.create({
    baseURL: "http://localhost:3000"
    // baseURL: "https://jsonplaceholder.typicode.com"
});


function getToken(){
    let token = Cookies.get("token");
    return (token) ? token : "";
}

export const api = {
    login: async (email, password) => {

        try {

            let response = await http.post("user/login", {
                token: getToken(),
                email: email,
                password: password
            });
        } catch(e) {
            console.log("Login error: " + e);
        }
        console.log(response.data);

        if (response.notallowed){

        }

        return response.data;
    }

};