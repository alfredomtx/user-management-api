import './App.css'
import { useState } from 'react'
import { connect } from 'react-redux'
import { MainRoutes } from './routes/MainRoutes';

import Footer from './components/partials/Footer'
import Header from './components/partials/Header'
import { Template } from './components/MainComponents';


function App(props) {

  return (
    <div>
      <Header />
      <MainRoutes/>
      <Footer />
    </div>
  )
}

const mapStateToProps = (state) => {
  return {
    user: state.user
  };
}

const mapDispatchToProps = (dispatch) => {
  return {

  };
}


export default connect(mapStateToProps, mapDispatchToProps)(App);
