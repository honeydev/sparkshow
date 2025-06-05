module Models.PageModel exposing (..)

import Pages.Index as IndexPage
import Models.Login as LoginPage

type PageModel
    = IndexPageModel IndexPage.PageModel
    | LoginPageModel LoginPage.PageModel
    | NotFoundPageModel

