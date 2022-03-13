import {Routes, Route, useRoutes} from 'react-router-dom'

import { NotFound } from '../pages/NotFound';
import { Home } from '../pages/Home'
import { About } from '../pages/About';
import { Login } from '../pages/Login';

export const MainRoutes = () => {

    return useRoutes([
        {path: "/", element: <Home />},
        {path: "/about", element: <About />},
        {path: "/login", element: <Login />},
        {path: "*", element: <NotFound />}
    ]);

}