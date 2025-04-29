package routes

import org.http4s.dsl.impl.QueryParamDecoderMatcher
object IdQueryParamMatcher extends QueryParamDecoderMatcher[Int]("id")
