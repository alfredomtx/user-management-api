import { useState } from "react";
import tw from "tailwind-styled-components"
import styled from 'styled-components';
import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

import { api } from "../api/OlxAPI"
import { doLogin } from "../helpers/AuthHandler";
import { useNavigate } from "react-router-dom";


// import { PageContainer, PageTitle } from "../components/MainComponents";

const Container = tw.div`
    p-4
    flex
    m-auto
    justify-center	
`;

const LoginForm = styled.form`
    background-color: #FFFF;
    border-radius: 3px;
    padding: 20px;
    box-shadow: 0px 0px 3px #999;

    display: flex;
    flex-direction: column;
    aling-items: center;

    div {
        padding-bottom: 1em;
    }

    label {
        text-align: right;
        padding-right: 20px;
    }
`
const Input = tw.input`
    w-full
    mt-1
    px-3
    py-2
    bg-white
    border
    border-slate-300
    rounded-md
    text-sm
    shadow-sm
    placeholder-slate-400
    ease-in-out duration-500
    focus:outline-none
    focus:border-sky-500
    focus:ring-1
    focus:ring-sky-500
    focus:invalid:border-pink-500
    focus:invalid:ring-pink-500

    invalid:border-pink-500 invalid:text-pink-600
    invalid:required:border-slate-300

    disabled:bg-slate-50 disabled:text-slate-500 disabled:border-slate-200 disabled:shadow-none
`

const LoginButton = tw.button`
    px-8
    py-2
    rounded
    rounded-full
    font-bold
    text-white
    bg-sky-500
    hover:bg-sky-700 
    disabled:opacity-50
`


export const Login = () => {
    const navigate = useNavigate();

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [rememberPassword, setRememberPassword] = useState(false);
    const [disabled, setDisabled] = useState(false);
    const [error, setError] = useState("");


    async function handleSubmit(e) {
        e.preventDefault();
        setDisabled(true);

        try {
            const response = await api.login(email, password);
        } catch(e) {
            doLogin("teste", rememberPassword);
            navigate("/");

            return loginError(e);
        }

        // login failed, do not continue
        if (response.error){
            return loginError(response.error);
        }

        doLogin(response.token, rememberPassword);
        navigate("/");
    }

    function loginError(errorMessage){
        setError(errorMessage);
        toast.error(errorMessage);
        setTimeout(() => setDisabled(false), 200);
    }

    return (
        <Container >

            <ToastContainer pauseOnFocusLoss={false} position="bottom-right" autoClose={3000}/>

            <LoginForm onSubmit={handleSubmit}>
                <h1 className="mb-6">Login</h1>
                <div>
                    <label>E-mail</label>
                <div>
                    <Input type="email" minLength="5" placeholder="example@example.com" required
                        value={email} disabled={disabled}
                        onChange={(e) => setEmail(e.target.value)}/>
                </div>
                </div>
                <div>
                    <label>Password</label>
                    <Input type="password" minLength="4"  placeholder="******" required
                        value={password} disabled={disabled}
                        onChange={(e) => setPassword(e.target.value)}/>
                </div>
                <div>
                    <input className="w-8" type="checkbox"
                        value={rememberPassword} disabled={disabled} checked={rememberPassword}
                        onChange={(e) => {setRememberPassword(!rememberPassword)}}/>
                    <label htmlFor="rememberPassword">Remember password</label>
                </div>
                <div>
                    <LoginButton disabled={disabled}>Login</LoginButton>
                </div>
            </LoginForm>
        </Container>

    )
    
}