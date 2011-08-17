#ifndef mugen_converter_triggers
#define mugen_converter_triggers

#include <string>

namespace Ast{
    class Value;
}

namespace Mugen{

/*! Trigger Handling converts triggers into python code */
namespace TriggerHandler{

/* Convert trigger identifier */
const std::string convert(const Ast::Value &);

}
}
#endif