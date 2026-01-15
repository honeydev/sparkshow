module Msg exposing (..)

import Browser
import Login as LoginPage
import Url


type Msg
    = LinkClicked Browser.UrlRequest
    | UrlChanged Url.Url
    | LoginMessage LoginPage.Msg
    | SessionLoaded String
    | SignOut
