module SqlBuilder exposing (..)

import Browser
import Html exposing (Html, button, div, input, option, select, span, text)
import Html.Attributes exposing (..)
import Html.Events exposing (onClick, onInput)


type InputField
    = ValidField String
    | InvalidField String String


type Msg
    = AddJoin
    | DeleteJoin
    | ChangeJoinField Int String
    | ChangeSelectField String
    | ChangeWhereField String


type FormState
    = Valid
    | Invalid
    | Unknown


type alias Input =
    Result String ( String, String )


type alias Model =
    { selectField : Input
    , whereField : Input
    , joinFields : List Input
    , formState : FormState
    , errors : Maybe (List String)
    }



-- main =
--     Browser.sandbox
--         { init =
--             { selectField = Ok ""
--             , whereField = Ok ""
--             , joinFields = []
--             , formState = Unknown
--             , errors = Nothing
--             }
--         , update = update
--         , view = view
--         }


fieldValue f =
    case f of
        Ok v ->
            v

        Err ( v, err ) ->
            v


fieldError f =
    case f of
        Ok v ->
            ""

        Err ( v, err ) ->
            err


validateSelect v =
    if String.length v > 1 then
        Ok v

    else
        Err ( v, "So short" )


validateWhere v =
    Err ( v, "Where err" )


validateJoin v =
    Ok v


update msg model =
    case msg of
        ChangeSelectField select ->
            { model | selectField = validateSelect select }

        ChangeWhereField whereQ ->
            { model | whereField = validateWhere whereQ }

        AddJoin ->
            { model | joinFields = model.joinFields ++ [ Ok "" ] }

        DeleteJoin ->
            { model | joinFields = List.take (List.length model.joinFields - 1) model.joinFields }

        ChangeJoinField index queryPart ->
            { model
                | joinFields =
                    List.indexedMap
                        (\i f ->
                            if i == index then
                                validateJoin queryPart

                            else
                                f
                        )
                        model.joinFields
            }


queryView model =
    div []
        [ span [] [ text ("SELECT " ++ fieldValue model.selectField ++ " FROM table_name" ++ "WHERE " ++ fieldValue model.whereField) ]
        ]


selectView model =
    div []
        [ span [] [ text "SELECT " ]
        , input [ placeholder "Select field", value (fieldValue model.selectField), onInput ChangeSelectField ] []
        ]


extractErrors model =
    let
        fields =
            [ model.selectField, model.whereField ]

        joins =
            []
    in
    List.filter
        (\f ->
            case f of
                Err ( v, err ) ->
                    True

                _ ->
                    False
        )
        fields
        |> List.map fieldError


view model =
    div []
        [ div []
            [ div [] [ text (extractErrors model |> String.join ", ") ]
            , selectView model
            , div []
                [ span [] [ text "FROM " ]
                , select [] [ option [ value "a" ] [ text "a" ], option [ value "x" ] [ text "x" ] ]
                ]
            , div []
                [ span [] [ text "WHERE " ]
                , input [ placeholder "Where field", value <| fieldValue model.whereField, onInput ChangeWhereField ] []
                ]
            , div []
                [ button [ onClick AddJoin ] [ text "+" ]
                , button [ onClick DeleteJoin ] [ text "-" ]
                , div []
                    (List.indexedMap (\i x -> input [ placeholder "Join field", value <| fieldValue x, onInput (ChangeJoinField i) ] []) model.joinFields)
                ]
            , queryView model
            ]
        ]
