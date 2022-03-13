import tw from "tailwind-styled-components"
import styled from 'styled-components';

import { Link } from "react-router-dom";
import { doLogout, isLogged } from '../../helpers/AuthHandler';

const HeaderStyle = tw.header`
    h-18
    bg-slate-400
    bg-white
    border-b-2
    w-full;
`;

const Container = tw.div`
    flex
    m-auto
    max-w-5xl
`;


const Logo = tw.div`
    flex
    flex-1
    items-center
    text-5xl
    font-bold
    h-12
`;

const Logo1 = tw.span`
    text-red-500
`;
const Logo2 = tw.span`
    text-green-500
`;
const Logo3 = tw.span`
    text-blue-500
`;

const Nav = styled.nav`
    padding-top: 10px;
    padding-bottom: 10px;

    ul, li {
        margin: 0;
        padding: 0;
    }
    ul {
        display: flex;
        align-items:center;
        height: 40px;
    }
    li {
        margin-left: 20px;
        margin-right: 20px;

        a, button {
            color: #000;
            font-size: 18px;

            &:hover {
                color: #999
            }

            &.button {
                background-color: #FF8100;
                border-radius: 4px;
                color: #FFF;
                padding: 5px 10px;
            }

            &.button:hover {
                background-color: #E57706;
            }
        }
    }
`;

export const Header = () => {

    let logged = isLogged();

    function logout(){
        doLogout();
        window.location.href = "/login";
    }

    return (
        <HeaderStyle>
            <Container>
                <Logo>
                    <Link to="/">
                        <Logo1>O</Logo1>
                        <Logo2>L</Logo2>
                        <Logo3>X</Logo3>
                    </Link>
                </Logo>
                <Nav>
                    <ul>
                        {logged && 
                            <>
                                <li><Link to="/my-account">My account</Link></li>
                                <li><button onClick={logout}>Logout</button></li>
                                <li><Link to="/postadd" className="button">Post add</Link></li>
                            </>
                        
                        }
                        {!logged && 
                            <>
                                <li><Link to="/login">Login</Link></li>
                                <li><Link to="/signout">Sign Up</Link></li>
                                <li><Link to="/login" className="button">Post add</Link></li>
                            </>
                        }
                        
                    </ul>
                </Nav>
            </Container>
        </HeaderStyle>
    )
    
}

export default Header;
